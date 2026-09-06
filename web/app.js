const nameInput=document.getElementById("name-input");
const message=document.getElementById("message");
const surnameInput = document.getElementById("surname-input");
document.getElementById("btn-yes").addEventListener("click", function() {
    const surname = surnameInput.value.trim();
    const name=nameInput.value.trim();
    if (surname === "" || name === "") {
        message.textContent = "Введите фамилию и имя!";
        return;
    }
    fetch("/attend?name=" + encodeURIComponent(name) + "&surname=" + encodeURIComponent(surname))
    .then(function(response) {return response.json();})
    .then(function(data) {
        if(data.result==="ok") {
            message.textContent="Спасибо, "+name+"! Ждите подтверждения учителя.";
        } else {
            message.textContent="Ошибка: "+data.message;
        }
    })
    .catch(function() {
        message.textContent="Сервер недоступен";
    });
});
document.getElementById("btn-no").addEventListener("click", function() {
    const surname = surnameInput.value.trim();
    const name=nameInput.value.trim();
    if(name===""||surname==="") {
        message.textContent="Сначала введите имя!";
        return;
    }
    fetch("/review?name=" + encodeURIComponent(name) + "&surname=" + encodeURIComponent(surname) + "&decision=REJECTED")
    .then(function(response) {return response.json();})
    .then(function(data) {
        if(data.result==="ok") {
            message.textContent="Принято, "+name;
        } else {
            message.textContent="Ошибка: "+data.message;
        }
    })
    .catch(function() {
        message.textContent="Сервер недоступен";
    });
});