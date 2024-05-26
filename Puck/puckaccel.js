setInterval(function() {
  Bluetooth.println(JSON.stringify(Puck.accel()));
}, 50);