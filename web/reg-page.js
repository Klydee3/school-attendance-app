const messageBox=document.getElementById("message");
const message={
    _timer:null,
    get textContent() {return messageBox.textContent;},
    set textContent(text) {
        messageBox.textContent=text;
        clearTimeout(this._timer);
        if (text!=="") {
            this._timer=setTimeout(function() {messageBox.textContent="";},2500);
        }
    }
};
document.getElementById("back-btn").addEventListener("click",function(){
	location.href="/index.html";
});
document.getElementById("register-btn").addEventListener("click",function() {
	const surname=document.getElementById("register-surname").value.trim();
    const name=document.getElementById("register-name").value.trim();
	const password=document.getElementById("register-password-input").value.trim(); 
	const passwordRepeat=document.getElementById("register-password-input-repeat").value.trim();
	const role=document.getElementById("role").value.trim();
	if (surname===""||name==="") {
        message.textContent="Введите фамилию и имя";
        return;
    }
	if (password!==passwordRepeat) {
		message.textContent="Пароли не совпадают!";
		return;
	}
	fetch("/api/teacher-n-admin-register",{
        method:"POST",
        headers:{"Content-Type":"application/x-www-form-urlencoded"},
        body:"surname="+encodeURIComponent(surname)+
			"&name="+encodeURIComponent(name)+
			"&password="+encodeURIComponent(password)+
			"&role="+encodeURIComponent(role)
    })
        .then(function(r) {return r.json();})
        .then(function(data) {
            if (data.result==="ok") {
                message.textContent=data.message;
				setTimeout(function() {location.href="/index.html";},2000);
            } else {
                message.textContent="Ошибка: "+data.message;
            }
        })
        .catch(function() {message.textContent="Сервер недоступен";});
});