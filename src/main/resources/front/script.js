var socket = new SockJS('http://localhost:8080/ws');
var stompClient = Stomp.over(socket);

stompClient.connect({}, function (frame) {
    console.log('Connected: ' + frame);
    stompClient.subscribe('/topic/messages', function (message) {
        showMessage(JSON.parse(message.body)["response"], false);
    });
});

function sendMessage() {
    var messageContent = document.getElementById("inputMessage").value;
    if(messageContent) {
        showMessage(messageContent, true);
        stompClient.send("/app/chat", {}, messageContent);
        document.getElementById("inputMessage").value = '';
    }
}



function showMessage(message, isClient) {
    var messageElement = document.createElement('div');
    var text = document.createElement('p');
    var date = document.createElement('span');
    var dateValue = new Date();
    date.innerText = dateValue.getHours() + ":" + dateValue.getMinutes();
    text.innerText = message;
    messageElement.appendChild(text);
    messageElement.appendChild(date);
    if (isClient) {
        messageElement.classList.add("messages_client");
    } else {
        messageElement.classList.add("messages_server");
    }
    document.getElementById("messages").appendChild(messageElement);
}





































