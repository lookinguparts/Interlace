package art.lookingup.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.jogamp.opengl.GL3;
import heronarts.lx.LX;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.Set;
import java.util.HashSet;

import static com.jogamp.opengl.GL2ES2.GL_COMPILE_STATUS;
import static com.jogamp.opengl.GL2ES2.GL_LINK_STATUS;

/**
 * Manages compiled shader caching for VShader patterns to improve Chromatik startup performance.
 * Caches compiled OpenGL shader programs and associated metadata in the VShaders/cache directory.
 */
public class ShaderCache {
    private static final Logger logger = Logger.getLogger(ShaderCache.class.getName());
    
    private static ShaderCache instance;
    private final String cacheDir;
    private final String manifestPath;
    private final Gson gson = new Gson();
    private CacheManifest manifest;
    
    // Cache statistics
    private int cacheHits = 0;
    private int cacheMisses = 0;
    
    /**
     * Represents a cached shader entry with metadata and dependency information
     */
    public static class CacheEntry {
        public String shaderName;
        public String sourceChecksum;
        public long lastModified;
        public Map<String, String> dependencies; // filename -> checksum
        public Map<String, Integer> uniformLocations;
        public JsonObject isfMetadata;
        public byte[] programBinary;
        public int programFormat;
        public boolean isValid;
        
        public CacheEntry() {
            dependencies = new HashMap<>();
            uniformLocations = new HashMap<>();
            isValid = true;
        }
    }
    
    /**
     * Manages cache manifest with dependency tracking
     */
    public static class CacheManifest {
        public Map<String, CacheEntry> entries;
        public String glVendor;
        public String glRenderer;
        public String glVersion;
        public long manifestVersion;
        
        public CacheManifest() {
            entries = new ConcurrentHashMap<>();
            manifestVersion = System.currentTimeMillis();
        }
    }
    
    /**
     * Result of loading a cached shader containing both the cache entry and the OpenGL program ID
     */
    public static class CachedShaderResult {
        public CacheEntry entry;
        public int programId;
        
        public CachedShaderResult(CacheEntry entry, int programId) {
            this.entry = entry;
            this.programId = programId;
        }
    }
    
    private ShaderCache(LX lx) {
        this.cacheDir = GLUtil.shaderDir(lx) + File.separator + "cache";
        this.manifestPath = cacheDir + File.separator + "manifest.json";
        initializeCacheDirectory();
        loadManifest();
    }
    
    /**
     * Get the singleton instance of ShaderCache
     */
    public static synchronized ShaderCache getInstance(LX lx) {
        if (instance == null) {
            instance = new ShaderCache(lx);
        }
        return instance;
    }
    
    /**
     * Initialize the cache directory structure
     */
    private void initializeCacheDirectory() {
        try {
            LX.log("Initializing cache directory: " + cacheDir);
            Path cachePath = Paths.get(cacheDir);
            if (!Files.exists(cachePath)) {
                Files.createDirectories(cachePath);
                LX.log("Created shader cache directory: " + cacheDir);
            } else {
                LX.log("Cache directory already exists: " + cacheDir);
            }
        } catch (IOException e) {
            LX.log("Failed to create cache directory: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Load the cache manifest from disk
     */
    private void loadManifest() {
        try {
            if (Files.exists(Paths.get(manifestPath))) {
                String manifestJson = new String(Files.readAllBytes(Paths.get(manifestPath)));
                manifest = gson.fromJson(manifestJson, CacheManifest.class);
                LX.log("Loaded shader cache manifest with " + manifest.entries.size() + " entries");
            } else {
                manifest = new CacheManifest();
                LX.log("Created new shader cache manifest");
            }
        } catch (Exception e) {
            LX.log("Failed to load manifest, creating new one: " + e.getMessage());
            manifest = new CacheManifest();
        }
    }
    
    /**
     * Save the cache manifest to disk
     */
    private void saveManifest() {
        try {
            String manifestJson = gson.toJson(manifest);
            Files.write(Paths.get(manifestPath), manifestJson.getBytes());
        } catch (IOException e) {
            LX.log("Failed to save manifest: " + e.getMessage());
        }
    }
    
    /**
     * Check if a cached shader is valid and up-to-date
     */
    public boolean isCacheValid(String shaderName, String shaderDir) {
        CacheEntry entry = manifest.entries.get(shaderName);
        if (entry == null || !entry.isValid) {
            return false;
        }
        
        try {
            // Check main shader file
            String shaderPath = shaderDir + File.separator + shaderName + ".vtx";
            if (!Files.exists(Paths.get(shaderPath))) {
                return false;
            }
            
            long currentModified = Files.getLastModifiedTime(Paths.get(shaderPath)).toMillis();
            if (currentModified > entry.lastModified) {
                return false;
            }
            
            // Check dependency files
            for (Map.Entry<String, String> dep : entry.dependencies.entrySet()) {
                String depPath = dep.getKey();
                String expectedChecksum = dep.getValue();
                
                if (!Files.exists(Paths.get(depPath))) {
                    return false;
                }
                
                String currentChecksum = calculateFileChecksum(depPath);
                if (!expectedChecksum.equals(currentChecksum)) {
                    return false;
                }
            }
            
            return true;
            
        } catch (Exception e) {
            LX.log("Error checking cache validity: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Load a cached shader program and return the entry with the OpenGL program ID
     */
    public CachedShaderResult loadCachedShader(String shaderName, GL3 gl) {
        CacheEntry entry = manifest.entries.get(shaderName);
        if (entry == null || !entry.isValid) {
            cacheMisses++;
            return null;
        }
        
        try {
            // Load binary cache file
            String cacheFile = cacheDir + File.separator + shaderName + ".cache";
            if (!Files.exists(Paths.get(cacheFile))) {
                LX.log("Cache file missing for " + shaderName + ", invalidating entry");
                invalidateShader(shaderName);
                cacheMisses++;
                return null;
            }
            
            byte[] cacheData = Files.readAllBytes(Paths.get(cacheFile));
            if (cacheData.length == 0) {
                LX.log("Empty cache file for " + shaderName + ", invalidating entry");
                invalidateShader(shaderName);
                cacheMisses++;
                return null;
            }
            
            entry.programBinary = cacheData;
            
            // Create OpenGL program from cached binary
            int programId = gl.glCreateProgram();
            
            if (entry.programBinary != null && entry.programBinary.length > 0) {
                ByteBuffer binaryBuffer = ByteBuffer.wrap(entry.programBinary);
                gl.glProgramBinary(programId, entry.programFormat, binaryBuffer, entry.programBinary.length);
                
                // Verify the program loaded correctly
                IntBuffer status = IntBuffer.allocate(1);
                gl.glGetProgramiv(programId, GL_LINK_STATUS, status);
                if (status.get(0) != 1) {
                    LX.log("Cached shader binary invalid for " + shaderName + ", will recompile");
                    gl.glDeleteProgram(programId);
                    invalidateShader(shaderName);
                    cacheMisses++;
                    return null;
                }
                
                // Validate program can be used
                gl.glValidateProgram(programId);
                gl.glGetProgramiv(programId, GL3.GL_VALIDATE_STATUS, status);
                if (status.get(0) != 1) {
                    LX.log("Cached shader program validation failed for " + shaderName + ", will recompile");
                    gl.glDeleteProgram(programId);
                    invalidateShader(shaderName);
                    cacheMisses++;
                    return null;
                }
            }
            
            cacheHits++;
            LX.log("Successfully loaded cached shader: " + shaderName);
            return new CachedShaderResult(entry, programId);
            
        } catch (Exception e) {
            LX.log("Error loading cached shader " + shaderName + ": " + e.getMessage());
            // Invalidate corrupted cache entry
            try {
                invalidateShader(shaderName);
            } catch (Exception invalidateEx) {
                LX.log("Failed to invalidate corrupted cache entry: " + invalidateEx.getMessage());
            }
            cacheMisses++;
            return null;
        }
    }
    
    /**
     * Cache a compiled shader program
     */
    public void cacheShader(String shaderName, String shaderDir, int programId, 
                           Map<String, Integer> uniformLocations, JsonObject isfMetadata, 
                           Set<String> dependencies, GL3 gl) {
        try {
            CacheEntry entry = new CacheEntry();
            entry.shaderName = shaderName;
            entry.uniformLocations = new HashMap<>(uniformLocations);
            entry.isfMetadata = isfMetadata;
            
            // Calculate checksums for main file and dependencies
            String mainShaderPath = shaderDir + File.separator + shaderName + ".vtx";
            entry.sourceChecksum = calculateFileChecksum(mainShaderPath);
            entry.lastModified = Files.getLastModifiedTime(Paths.get(mainShaderPath)).toMillis();
            
            // Store dependency checksums
            for (String depPath : dependencies) {
                String checksum = calculateFileChecksum(depPath);
                entry.dependencies.put(depPath, checksum);
            }
            
            // Check if program binary is supported
            IntBuffer binaryFormats = IntBuffer.allocate(1);
            gl.glGetIntegerv(GL3.GL_NUM_PROGRAM_BINARY_FORMATS, binaryFormats);
            LX.log("Number of supported binary formats: " + binaryFormats.get(0));
            
            // Get program binary
            IntBuffer binaryLength = IntBuffer.allocate(1);
            gl.glGetProgramiv(programId, GL3.GL_PROGRAM_BINARY_LENGTH, binaryLength);
            LX.log("Program binary length for " + shaderName + ": " + binaryLength.get(0));
            
            if (binaryLength.get(0) > 0) {
                ByteBuffer binaryBuffer = ByteBuffer.allocate(binaryLength.get(0));
                IntBuffer binaryFormat = IntBuffer.allocate(1);
                IntBuffer actualLength = IntBuffer.allocate(1);
                
                gl.glGetProgramBinary(programId, binaryLength.get(0), actualLength, binaryFormat, binaryBuffer);
                
                int actualLen = actualLength.get(0);
                LX.log("Actual program binary length: " + actualLen);
                
                if (actualLen > 0) {
                    entry.programBinary = new byte[actualLen];
                    binaryBuffer.rewind(); // Reset buffer position
                    binaryBuffer.get(entry.programBinary);
                    entry.programFormat = binaryFormat.get(0);
                    
                    // Save binary to cache file
                    String cacheFile = cacheDir + File.separator + shaderName + ".cache";
                    Files.write(Paths.get(cacheFile), entry.programBinary);
                    
                    // Update manifest
                    manifest.entries.put(shaderName, entry);
                    saveManifest();
                    
                    LX.log("Successfully cached compiled shader: " + shaderName + " (" + entry.programBinary.length + " bytes)");
                } else {
                    LX.log("Program binary retrieval failed for " + shaderName + " - no data returned");
                }
            } else {
                LX.log("Program binary not supported or shader not linked properly for " + shaderName);
                // Even if we can't cache the binary, we can still cache the metadata
                manifest.entries.put(shaderName, entry);
                saveManifest();
                LX.log("Cached shader metadata (no binary) for: " + shaderName);
            }
            
        } catch (Exception e) {
            LX.log("Failed to cache shader " + shaderName + ": " + e.getMessage());
        }
    }
    
    /**
     * Calculate SHA-256 checksum of a file
     */
    private String calculateFileChecksum(String filePath) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] fileBytes = Files.readAllBytes(Paths.get(filePath));
            byte[] hash = digest.digest(fileBytes);
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException | IOException e) {
            LX.log("Failed to calculate checksum for " + filePath + ": " + e.getMessage());
            return "";
        }
    }
    
    /**
     * Clear all cached shaders
     */
    public void clearCache() {
        try {
            // Delete all cache files
            File cacheDirectory = new File(cacheDir);
            if (cacheDirectory.exists()) {
                File[] files = cacheDirectory.listFiles();
                if (files != null) {
                    for (File file : files) {
                        if (file.getName().endsWith(".cache")) {
                            file.delete();
                        }
                    }
                }
            }
            
            // Clear manifest
            manifest = new CacheManifest();
            saveManifest();
            
            // Reset statistics
            cacheHits = 0;
            cacheMisses = 0;
            
            LX.log("Cleared shader cache");
            
        } catch (Exception e) {
            LX.log("Failed to clear cache: " + e.getMessage());
        }
    }
    
    /**
     * Remove a specific shader from cache
     */
    public void invalidateShader(String shaderName) {
        try {
            manifest.entries.remove(shaderName);
            
            String cacheFile = cacheDir + File.separator + shaderName + ".cache";
            Files.deleteIfExists(Paths.get(cacheFile));
            
            saveManifest();
            LX.log("Invalidated cached shader: " + shaderName);
            
        } catch (Exception e) {
            LX.log("Failed to invalidate shader " + shaderName + ": " + e.getMessage());
        }
    }
    
    /**
     * Get cache statistics
     */
    public String getCacheStats() {
        int totalEntries = manifest.entries.size();
        int totalRequests = cacheHits + cacheMisses;
        double hitRate = totalRequests > 0 ? (double) cacheHits / totalRequests * 100 : 0;
        
        return String.format("Cache: %d entries, %d hits, %d misses (%.1f%% hit rate)", 
                           totalEntries, cacheHits, cacheMisses, hitRate);
    }
    
    /**
     * Check if OpenGL context has changed (different driver/version)
     */
    public boolean isGLContextValid(GL3 gl) {
        String currentVendor = gl.glGetString(GL3.GL_VENDOR);
        String currentRenderer = gl.glGetString(GL3.GL_RENDERER);
        String currentVersion = gl.glGetString(GL3.GL_VERSION);
        
        if (manifest.glVendor == null) {
            // First time - store current context info
            manifest.glVendor = currentVendor;
            manifest.glRenderer = currentRenderer;
            manifest.glVersion = currentVersion;
            saveManifest();
            return true;
        }
        
        return Objects.equals(manifest.glVendor, currentVendor) &&
               Objects.equals(manifest.glRenderer, currentRenderer) &&
               Objects.equals(manifest.glVersion, currentVersion);
    }
}