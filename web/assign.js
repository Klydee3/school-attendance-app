if(!localStorage.getItem("token")||(localStorage.getItem("role")!=="ADMIN"&&localStorage.getItem("role")!=="MAIN_DEVELOPER")){
    location.href="/index.html";
}
function loadTeachers(){
    fetch("/api/teachers-list",{headers:{"Authorization":"Bearer "+localStorage.getItem("token")}})
    .then(function(r){return r.json();})
    .then(function(data){
        if(data.result!=="ok"){
            document.getElementById("message").textContent="Ошибка: "+data.message;
            return;
        }
        const box=document.getElementById("teachers-box");
        box.textContent="";
        if(data.teachers.length===0){
            box.textContent="Учителей пока нет.";
            return;
        }
        data.teachers.forEach(function(t){
            const row=document.createElement("div");
            row.className="bubble";
            const input=document.createElement("input");
            input.className="name-input";
            input.placeholder="Класс (1-11)";
            input.value=t.class;
            const btn=document.createElement("button");
            btn.className="btn";
            btn.textContent="Сохранить";
            btn.addEventListener("click",function(){
                fetch("/api/assign-class",{
                    method:"POST",
                    headers:{"Content-Type":"application/x-www-form-urlencoded","Authorization":"Bearer "+localStorage.getItem("token")},
                    body:"teacher="+encodeURIComponent(t.login)+"&class="+encodeURIComponent(input.value)
                })
                .then(function(r){return r.json();})
                .then(function(d){
                    document.getElementById("message").textContent=d.result==="ok"?"Сохранено.":"Ошибка: "+d.message;
                })
                .catch(function(){document.getElementById("message").textContent="Сервер недоступен";});
            });
            row.appendChild(document.createTextNode(t.login));
            row.appendChild(input);
            row.appendChild(btn);
            box.appendChild(row);
        });
    })
    .catch(function(){document.getElementById("message").textContent="Сервер недоступен";});
}
loadTeachers();
document.getElementById("back-btn").addEventListener("click",function(){
    location.href="/admin.html";
});