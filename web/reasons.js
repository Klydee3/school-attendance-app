if(!localStorage.getItem("token")||localStorage.getItem("role")==="STUDENT"){
    location.href="/index.html";
}
const date=new URLSearchParams(location.search).get("date")||"";
fetch("/api/summary?date="+encodeURIComponent(date),{headers:{"Authorization":"Bearer "+localStorage.getItem("token")}})
.then(function(r){return r.json();})
.then(function(data){
    if(data.result!=="ok"){
        document.getElementById("message").textContent="Ошибка: "+data.message;
        return;
    }
    document.getElementById("reasons-date").textContent="Дата: "+data.date;
    const box=document.getElementById("reasons-list");
    box.textContent="";
    if(!data.absents||data.absents.length===0){
        box.textContent="Никто не сообщил об отсутствии.";
        return;
    }
    data.absents.forEach(function(a){
        const div=document.createElement("div");
        div.className="bubble";
        div.textContent=a.name+" ("+a.class+") — "+(a.reason===""?"причина не указана":a.reason);
        box.appendChild(div);
    });
})
.catch(function(){document.getElementById("message").textContent="Сервер недоступен";});
document.getElementById("back-btn").addEventListener("click",function(){
    location.href="/summary.html";
});