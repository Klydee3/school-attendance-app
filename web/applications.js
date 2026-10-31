if(!localStorage.getItem("token")||(localStorage.getItem("role")!=="ADMIN"&&localStorage.getItem("role")!=="MAIN_DEVELOPER")){
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
            this._timer=setTimeout(function(){messageBox.textContent="";},2000);
        }
    }
};
function loadPendingAccounts(){
    fetch("/api/pending-accounts",{headers:{"Authorization":"Bearer "+localStorage.getItem("token")}})
    .then(function(r){return r.json();})
    .then(function(data){
        const box=document.getElementById("pending-accounts");
		const role=localStorage.getItem("role");
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
			div.className="bubble";
			if(row.role=="TEACHER"){
				row.role="Учитель";
			}else if(row.role=="ADMIN"){
				row.role="Администрация";
			}else{
				row.role="Столовая";
			}
            div.textContent=row.login+" ("+row.role+"); ";
			if(role==="MAIN_DEVELOPER"){
				div.textContent=row.login+" ("+row.role+"; "+row.school+"); ";
			}
            const btn=document.createElement("button");
            btn.className="btn";
            btn.textContent="Подтвердить";
            btn.addEventListener("click",function(){approveAccount(row.login);});
            div.appendChild(btn);
            box.appendChild(div);
        });
    })
    .catch(function(e){
		message.textContent="Сервер недоступен";});
}
function approveAccount(login){
    fetch("/api/approve-account",{
        method:"POST",
        headers:{"Content-Type":"application/x-www-form-urlencoded","Authorization":"Bearer "+localStorage.getItem("token")},
        body:"login="+encodeURIComponent(login)
    })
    .then(function(r){return r.json();})
    .then(function(data) {
        if(data.result==="ok") {
            message.textContent="Аккаунт подтверждён.";
            loadPendingAccounts();
        } else {
            message.textContent="Ошибка: "+data.message;
        }
    })
    .catch(function() {message.textContent="Сервер недоступен";});
}
document.getElementById("back-btn").addEventListener("click", function() {
    location.href="/admin.html";
});
loadPendingAccounts();