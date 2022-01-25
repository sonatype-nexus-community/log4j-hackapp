var socket = new WebSocket("ws://" + location.host + "/ws");


function clickAll(cls) {

var elements = document.getElementsByClassName(cls);
       for(var i=0;i < elements.length;i++){
         elements[i].click();
       }
}

socket.onopen = function(event) {

};

socket.onmessage = function(event) {
console.log(event.data);

  msg=JSON.parse(event.data);
  console.log(msg);
  if(msg.cmd=="update") {
    if(msg.target=="consoles") {
    var elements = document.getElementsByClassName('console');
       for(var i=0;i < elements.length;i++){
         elements[i].click();
       }
       return;
    }

    if(msg.target.startsWith("console")) {
        var e=document.getElementById("grid");
        if(e!=null) e.click();
    }

    var e=document.getElementById(msg.target);
    if(e!=null) e.click();

  }




};

socket.onclose = function(event) {

};

socket.onerror = function(error) {

};