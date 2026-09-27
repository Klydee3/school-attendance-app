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
function loadPendingAccounts(){
    fetch("/api/pending-accounts",{headers:{"Authorization":"Bearer "+localStorage.getItem("token")}})
    .then(function(r){return r.json();})
    .then(function(data){
        const box=document.getElementById("pending-accounts");
        box.textContent="";
        if(data.result!=="ok"){
            box.textContent="Ошибка: "+data.message;
            return;
        }
        if(data.rows.length===0){
            box.textContent="Новых заявок нет";
            return;
        }
        data.rows.forEach(function(row){
            const div=document.createElement("div");
            div.textContent=row.login+" ("+row.role+", "+row.school+") ";
            const btn=document.createElement("button");
            btn.className="btn";
            btn.textContent="Подтвердить";
            btn.addEventListener("click",function(){approveAccount(row.login);});
            div.appendChild(btn);
            box.appendChild(div);
        });
    })
    .catch(function(){message.textContent="Сервер недоступен";});
}
function approveAccount(login){
    fetch("/api/approve-account",{
        method:"POST",
        headers:{"Content-Type":"application/x-www-form-urlencoded","Authorization":"Bearer "+localStorage.getItem("token")},
        body:"login="+encodeURIComponent(login)
    })
    .then(function(r){return r.json();})
    .then(function(data){
        message.textContent=data.result==="ok"?"Аккаунт подтверждён.":"Ошибка: "+data.message;
        loadPendingAccounts();
    })
    .catch(function(){message.textContent="Сервер недоступен";});
}
document.getElementById("back-btn").addEventListener("click",function(){location.href="/admin.html";});
loadPendingAccounts();