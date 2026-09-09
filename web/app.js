const message=document.getElementById("message");
const loginScreen=document.getElementById("login-screen");
const studentScreen=document.getElementById("student-screen");
const teacherScreen=document.getElementById("teacher-screen");
const adminScreen=document.getElementById("admin-screen");
const logoutBtn=document.getElementById("logout-btn");
const changeBox=document.getElementById("change-box");
function showScreen() {
    const role=localStorage.getItem("role");
    loginScreen.style.display="none";
    studentScreen.style.display="none";
    teacherScreen.style.display="none";
    adminScreen.style.display="none";
    changeBox.style.display = "none";
    logoutBtn.style.display="none";
    if (role==="STUDENT") {
        studentScreen.style.display="block";
        logoutBtn.style.display="block";
    } else if(role==="TEACHER") {
        teacherScreen.style.display="block";
        logoutBtn.style.display="block";
        loadPending();
    } else if(role==="ADMIN") {
        adminScreen.style.display="block";
        logoutBtn.style.display="block"
        loadAll();
    } else {
        loginScreen.style.display="block";
    }
    if (role !== null) {
        changeBox.style.display = "block";
    }
}
document.getElementById("login-btn").addEventListener("click", function() {
    const login=document.getElementById("login-input").value.trim();
    const password=document.getElementById("password-input").value.trim();
    if (login===""||password==="") {
        message.textContent="Введите логин и пароль";
        return;
    }
    fetch("/login?login="+encodeURIComponent(login)+"&password="+encodeURIComponent(password))
        .then(function(r) { return r.json(); })
        .then(function(data) {
            if (data.result==="ok") {
                localStorage.setItem("token", data.token);
                localStorage.setItem("role", data.role);
                message.textContent="";
                showScreen();
            } else {
                message.textContent = "Ошибка: "+data.message;
            }
        })
        .catch(function() { message.textContent="Сервер недоступен"; });
});
function attend(answer) {
    const token=localStorage.getItem("token");
    fetch("/attend?token="+encodeURIComponent(token)+"&answer="+answer)
        .then(function(r) { return r.json(); })
        .then(function(data) {
            if (data.result==="ok") {
                message.textContent=answer==="yes"
                    ?"Спасибо! Ждите подтверждения учителя."
                    :"Принято. Порция не нужна.";
            } else {
                message.textContent="Ошибка: "+data.message;
            }
        })
        .catch(function() { message.textContent="Сервер недоступен"; });
}
document.getElementById("btn-yes").addEventListener("click", function() { attend("yes"); });
document.getElementById("btn-no").addEventListener("click", function() { attend("no"); });
logoutBtn.addEventListener("click", function() {
    localStorage.removeItem("token");
    localStorage.removeItem("role");
    showScreen();
});
function loadPending() {
    const token=localStorage.getItem("token");
    fetch("/students?token="+encodeURIComponent(token))
        .then(function(r) {return r.json();})
        .then(function(students) {
            if (!Array.isArray(students)) {
                message.textContent="Сессия устарела, войдите снова.";
                return;
            }
            const list=document.getElementById("pending-list");
            list.textContent="";
            students.forEach(function(s) {
                if (s.status!=="PENDING") {
                    return;
                }
                const row=document.createElement("div");
                row.className="pending-row";
                const label=document.createElement("span");
                label.textContent=s.surname+" "+s.name;
                const okBtn=document.createElement("button");
                okBtn.className="btn btn-yes";
                okBtn.textContent="Подтвердить";
                okBtn.addEventListener("click", function() { review(s,"APPROVED");});
                const noBtn=document.createElement("button");
                noBtn.className="btn btn-no";
                noBtn.textContent="Отклонить";
                noBtn.addEventListener("click",function() { review(s,"REJECTED");});
                row.appendChild(label);
                row.appendChild(okBtn);
                row.appendChild(noBtn);
                list.appendChild(row);
            });
            if (list.children.length===0) {
                list.textContent="Нет ожидающих подтверждения.";
            }
        })
        .catch(function() { message.textContent = "Сервер недоступен"; });
}
function review(student,decision) {
    const token=localStorage.getItem("token");
    fetch("/review?token="+encodeURIComponent(token)
    +"&name="+encodeURIComponent(student.name)
    +"&surname="+encodeURIComponent(student.surname)
    +"&decision="+decision)
    .then(function(r) {return r.json();})
    .then(function(data) {
        if(data.result==="ok") {
            loadPending();
        }else{
            message.textContent="Ошибка: "+data.message;
        }
    })
    .catch(function() {message.textContent="Сервер недоступен";});
}
function loadAll() {
    const token=localStorage.getItem("token");
    fetch("/students?token="+encodeURIComponent(token))
    .then(function(r) {return r.json();})
    .then(function(students) {
        if(!Array.isArray(students)) {
            message.textContent="Сессия устарела, войдите снова.";
            return;
        }
        const list=document.getElementById("all-list");
        list.textContent="";
        students.forEach(function(s) {
            const row=document.createElement("div");
            row.className="pending-row";
            const label=document.createElement("span");
            label.textContent=s.surname+" "+s.name+"---"+s.status;
            row.appendChild(label);
            list.appendChild(row);
        });
    })
    .catch(function() {message.textContent+"Сервер недоступен";});
}
document.getElementById("reg-btn").addEventListener("click",function() {
    const surname=document.getElementById("reg-surname").value.trim();
    const name=document.getElementById("reg-name").value.trim();
    if (surname===""||name ==="") {
        message.textContent="Введите фамилию и имя";
        return;
    }
    const token = localStorage.getItem("token");
    fetch("/register?token="+encodeURIComponent(token)
        +"&name="+encodeURIComponent(name)
        +"&surname="+encodeURIComponent(surname))
        .then(function(r) {return r.json();})
        .then(function(data) {
            if (data.result==="ok") {
                message.textContent="Ученик добавлен.";
                document.getElementById("reg-surname").value="";
                document.getElementById("reg-name").value="";
                loadAll();
            } else {
                message.textContent="Ошибка: "+data.message;
            }
        })
        .catch(function() { message.textContent = "Сервер недоступен"; });
});
document.getElementById("save-btn").addEventListener("click",function() {
    const token=localStorage.getItem("token");
    fetch("/save?token="+encodeURIComponent(token))
        .then(function(r) {return r.json();})
        .then(function(data) {
            message.textContent=data.result==="ok"?"Сохранено в файл.":"Ошибка: "+data.message;
        })
        .catch(function() {message.textContent="Сервер недоступен";});
});
document.getElementById("change-btn").addEventListener("click",function() {
    const oldPass=document.getElementById("old-pass").value.trim();
    const newPass=document.getElementById("new-pass").value.trim();
    if(oldPass===""||newPass==="") {
        message.textContent="Введите оба пароля";
        return;
    }
    const token=localStorage.getItem("token");
    fetch("/change-password?token="+encodeURIComponent(token)
        +"&oldPassword="+encodeURIComponent(oldPass)
        +"&newPassword="+encodeURIComponent(newPass))
        .then(function(r) {return r.json();})
        .then(function(data) {
            if (data.result==="ok") {
                message.textContent="Пароль изменён.";
                document.getElementById("old-pass").value="";
                document.getElementById("new-pass").value="";
            } else {
                message.textContent="Ошибка: "+data.message;
            }
        })
        .catch(function() {message.textContent="Сервер недоступен";});
});
showScreen();