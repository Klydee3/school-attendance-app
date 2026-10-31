if(!localStorage.getItem("token")||localStorage.getItem("role")!=="MAIN_DEVELOPER"){
    location.href="/index.html";
}
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
document.getElementById("regDev").addEventListener("click",function(){
	const name=document.getElementById("devName").value.trim();
	const pass=document.getElementById("devPass").value.trim();
	if(name===null||name===""||pass===null||pass===""){
		message.textContent="Пожалуйста, введите имя и пароль!";
		return;
	}
	fetch("/api/reg-dev",{
		method:"POST",
		headers:{"Content-type":"application/x-www-urlencoded",
		"Authorization":"Bearer "+localStorage.getItem("token")},
		body:"&name="+encodeURIComponent(name)+
			"&pass="+encodeURIComponent(pass)
	})
		.then(function(r){return r.json();})
		.then(function(data){
			if(data.result==="ok"){
				message.textContent=data.messsage;
			}else{
				message.textContent="Ошибка: "+data.message;
			}
		})
		.catch(function(){message.textContent="Сервер недоступен";});
});