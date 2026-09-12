const message=document.getElementById("message");
const token=localStorage.getItem("token");
const role=localStorage.getItem("role");
if(token===null||role!=="ADMIN") {
    location.href="index.html";
}
function loadAll() {
    fetch("/students?token="+encodeURIComponent(token))
    .then(function(r) {return r.json();})
    .then(function(students) {
        if(!Array.isArray(students)) {
            message.textContent="Сессия устарела, войдите снова";
            return;
        }
        const list=document.getElementById("all-list");
        list.textContent="";
        students.forEach(function(s) {
            const row=document.createElement("div");
            row.className="pending-row";
            const label=document.createElement("span");
            label.textContent=s.surname+" "+s.name+"---"+s.status;
            const delBtn=document.createElement("button");
            delBtn.className="btn btn-no";
            delBtn.textContent="Удалить";
            delBtn.addEventListener("click",function() {removeStudent(s);});
            row.appendChild(label);
            row.appendChild(delBtn);
            list.appendChild(row);
        });
    })
    .catch(function() {message.textContent="Сервер не доступен";});
}
function removeStudent(student) {
    fetch("/delete-student?token="+encodeURIComponent(token)
    +"&name="+encodeURIComponent(student.name)
    +"&surname="+encodeURIComponent(student.surname))
    .then(function(r){return r.json();})
    .then(function(data) {
        if(data.result==="ok") {
            message.textContent="Ученик удален.";
            loadAll();
        } else {
            message.textContent="Ошибка: "+data.message;
        }
    })
    .catch(function() {message.textContent="Сервер не доступен";});
}
document.getElementById("back-btn").addEventListener("click",function() {
    location.href="index.html";
});
loadAll();