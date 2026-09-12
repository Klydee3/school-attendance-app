const message=document.getElementById("message");
const token=localStorage.getItem("token");
if(token===null) {
    location.href="index.html";
}
document.getElementById("change-btn").addEventListener("click",function() {
    const oldPass=document.getElementById("old-pass").value.trim();
    const newPass=document.getElementById("new-pass").value.trim();
    if(oldPass===""||newPass==="") {
        message.textContent="Введите оба пароля";
        return;
    }
    const token=localStorage.getItem("token");
    fetch("/change-password?token="+encodeURIComponent(token)
        +"&oldPassword="+encodeURIComponent(oldPass)
        +"&newPassword="+encodeURIComponent(newPass))
        .then(function(r) {return r.json();})
        .then(function(data) {
            if (data.result==="ok") {
                message.textContent="Пароль изменён.";
                localStorage.removeItem("token");
                localStorage.removeItem("role");
                setTimeout(function() {location.href="index.html";},1500);
            } else {
                message.textContent="Ошибка: "+data.message;
            }
        })
        .catch(function() {message.textContent="Сервер недоступен";});
});
document.getElementById("back-btn").addEventListener("click",function(){
    location.href="index.html";
});