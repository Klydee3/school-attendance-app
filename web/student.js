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
function attend(answer){
    fetch("/api/attend?answer="+encodeURIComponent(answer),{
        method:"POST",
        headers:{"Authorization":"Bearer "+localStorage.getItem("token")}
    })
    .then(function(r){return r.json();})
    .then(function(data){
        message.textContent=data.result==="ok"?"Отметка сохранена.":"Ошибка: "+data.message;
    })
    .catch(function(){message.textContent="Сервер недоступен";});
}
document.getElementById("btn-yes").addEventListener("click",function(){attend("yes");});
document.getElementById("btn-no").addEventListener("click",function(){attend("no");});