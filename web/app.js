const messageBox=document.getElementById("message");
const message={
    _timer:null,
    get textContent() {return messageBox.textContent;},
    set textContent(text) {
        messageBox.textContent=text;
        clearTimeout(this._timer);
        if(text!=="") {
            this._timer=setTimeout(function() {messageBox.textContent=""; },5000);
        }
    }
};
function homeFor(role) {
    if(role==="STUDENT") {return "/student.html";}
    if(role==="TEACHER") {return "/teacher.html";}
	if(role==="CAFETERIA") {return "/summary.html";}
    return "/admin.html";
}
const token=localStorage.getItem("token");
const role=localStorage.getItem("role");
if(token&&role) {
    location.href=homeFor(role);
}
document.getElementById("to-reg-btn").addEventListener("click",function() {
    location.href="/reg-page.html";
});
document.getElementById("login-btn").addEventListener("click",function() {
    const login=document.getElementById("login-input").value.trim();
    const password=document.getElementById("password-input").value.trim();
    if(login===""||password==="") {
        message.textContent="Введите логин и пароль";
        return;
    }
    fetch("/api/login", {
        method:"POST",
        headers:{"Content-Type":"application/x-www-form-urlencoded"},
        body:"login="+encodeURIComponent(login)+"&password="+encodeURIComponent(password)
    })
    .then(function(r) {return r.json();})
    .then(function(data) {
        if(data.result==="ok") {
            localStorage.setItem("token",data.token);
            localStorage.setItem("role",data.role);
            location.href=homeFor(data.role);
        }else{
            message.textContent="Ошибка: "+data.message;
        }
    })
    .catch(function() {message.textContent="Сервер недоступен";});
});