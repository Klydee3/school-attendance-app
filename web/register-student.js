if(!localStorage.getItem("token")||localStorage.getItem("role")!=="ADMIN"){
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
document.getElementById("back-btn").addEventListener("click",function(){location.href="/admin.html";});
document.getElementById("reg-student-btn").addEventListener("click",function(){
    const surname=document.getElementById("reg-surname").value.trim();
    const name=document.getElementById("reg-name").value.trim();
    const className=document.getElementById("reg-class").value.trim();
    if(surname===""||name===""||className===""){
        message.textContent="Нужны фамилия, имя и класс";
        return;
    }
    fetch("/api/register",{
        method:"POST",
        headers:{"Content-Type":"application/x-www-form-urlencoded","Authorization":"Bearer "+localStorage.getItem("token")},
        body:"surname="+encodeURIComponent(surname)+"&name="+encodeURIComponent(name)+"&className="+encodeURIComponent(className)
    })
    .then(function(r){return r.json();})
    .then(function(data){
        if(data.result==="ok"){
            message.textContent="Ученик зарегистрирован: "+data.name;
            document.getElementById("reg-surname").value="";
            document.getElementById("reg-name").value="";
            document.getElementById("reg-class").value="";
        }else{
            message.textContent="Ошибка: "+data.message;
        }
    })
    .catch(function(){message.textContent="Сервер недоступен";});
});