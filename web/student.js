if(!localStorage.getItem("token")||localStorage.getItem("role")!=="STUDENT"){
    location.href="/index.html";
}
const messageBox=document.getElementById("message");
const message={
    _timer:null,
    get textContent(){return messageBox.textContent;},
    set textContent(text){
        messageBox.textContent=text;
        clearTimeout(this._timer);
        if(text!==""){
            this._timer=setTimeout(function(){messageBox.textContent="";},5000);
        }
    }
};
function attend(answer,reason){
    fetch("/api/attend",{
        method:"POST",
        headers:{"Content-Type":"application/x-www-form-urlencoded","Authorization":"Bearer "+localStorage.getItem("token")},
        body:"answer="+encodeURIComponent(answer)+"&reason="+encodeURIComponent(reason)
    })
    .then(function(r){return r.json();})
    .then(function(data){
        if(data.result==="ok"){
            message.textContent="Отметка сохранена.";
            setTimeout(function(){location.href="/student.html";},2000);
        }else{
            message.textContent=data.message;
        }
    })
    .catch(function(){message.textContent="Сервер недоступен";});
}
document.getElementById("btn-yes").addEventListener("click",function(){attend("yes","");});
document.getElementById("btn-no").addEventListener("click",function(){
	const reason=document.getElementById("reason-input").value.trim();
	if(reason==="") {
		message.textContent="Нужно обязательно ввести причину отсутствия!";
		return;
	}
	attend("no",reason);
});