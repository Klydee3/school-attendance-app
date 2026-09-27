if(!localStorage.getItem("token")||localStorage.getItem("role")!=="TEACHER"){
    location.href="/index.html";
}
fetch("/api/summary",{headers:{"Authorization":"Bearer "+localStorage.getItem("token")}})
.then(function(r){return r.json();})
.then(function(data){
    if(data.result==="ok"){
        document.getElementById("teacher-count").textContent="Сегодня присутствует "+data.presentAll+" из "+data.totalAll+" учеников.";
    }else{
        document.getElementById("teacher-count").textContent="Сводка недоступна: "+data.message;
    }
})
.catch(function(){document.getElementById("teacher-count").textContent="Сервер недоступен";});
document.getElementById("send-report-btn").addEventListener("click",function(){location.href="/summary.html";});