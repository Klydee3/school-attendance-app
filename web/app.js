const nameInput=document.getElementById("name-input");
const message=document.getElementById("message");
document.getElementById("btn-yes").addEventListener("click", function() {
    const name=nameInput.value.trim();
    if(name==="") {
        message.textContent="Сначала введите имя!";
        return;
    }
    fetch("/register?name="+encodeURIComponent(name))
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
    const name=nameInput.value.trim();
    if(name==="") {
        message.textContent="Сначала введите имя!";
        return;
    }
    fetch("/register?name="+encodeURIComponent(name)+"&decision=REJECTED")
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