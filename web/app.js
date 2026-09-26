const messageBox=document.getElementById("message");
const message={
    _timer:null,
    get textContent() {return messageBox.textContent;},
    set textContent(text) {
        messageBox.textContent=text;
        clearTimeout(this._timer);
        if (text!=="") {
            this._timer=setTimeout(function() {messageBox.textContent="";},2500);
        }
    }
};
const loginScreen=document.getElementById("login-screen");
const studentScreen=document.getElementById("student-screen");
const teacherScreen=document.getElementById("teacher-screen");
const adminScreen=document.getElementById("admin-screen");
const logoutBtn=document.getElementById("logout-btn");
const sessionButtons=document.getElementById("session-buttons");
const changePassButton=document.getElementById("change-pass-button");
const regScreen=document.getElementById("reg-screen");
function showScreen() {
	message.textContent="";
    const role=localStorage.getItem("role");
    loginScreen.style.display="none";
    studentScreen.style.display="none";
    teacherScreen.style.display="none";
    adminScreen.style.display="none";
    sessionButtons.style.display="none";
    if (role==="STUDENT") {
        studentScreen.style.display="block";
    } else if(role==="TEACHER") {
        teacherScreen.style.display="block";
        loadPending();
    } else if(role==="ADMIN") {
        adminScreen.style.display="block";
    } else {
        loginScreen.style.display="block";
    }
    if(role!==null) {
        sessionButtons.style.display="flex";
    }
	if(role==="ADMIN"&&document.getElementById("pending-accounts")){
		loadPendingAccounts();
	}
}
document.getElementById("login-btn").addEventListener("click", function() {
    const login=document.getElementById("login-input").value.trim();
    const password=document.getElementById("password-input").value.trim();
    if (login===""||password==="") {
        message.textContent="Введите логин и пароль";
        return;
    }
    fetch("/api/login",{
        method:"POST",
        headers:{"Content-Type":"application/x-www-form-urlencoded"},
        body:"login="+encodeURIComponent(login)+"&password="+encodeURIComponent(password)
    })
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
    fetch("/api/attend?answer="+encodeURIComponent(answer),
        {headers:{"Authorization":"Bearer "+token}})
        .then(function(r) { return r.json(); })
        .then(function(data) {
            if (data.result==="ok") {
                message.textContent=answer==="yes"
                    ?"Спасибо! Ждите подтверждения учителя."
                    :"Принято";
            } else {
                message.textContent="Ошибка: "+data.message;
            }
        })
        .catch(function() { message.textContent="Сервер недоступен"; });
}
document.getElementById("btn-yes").addEventListener("click", function() {attend("yes");});
document.getElementById("btn-no").addEventListener("click", function() {attend("no");});
logoutBtn.addEventListener("click", function() {
    localStorage.removeItem("token");
    localStorage.removeItem("role");
    showScreen();
});
function loadPending() {
    const token=localStorage.getItem("token");
    fetch("/api/students",{headers:{"Authorization":"Bearer "+token}})
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
    fetch("/api/review?name="+encodeURIComponent(student.name)+"&surname="+encodeURIComponent(student.surname)
    +"&decision="+encodeURIComponent(decision),
    {headers:{"Authorization":"Bearer "+token}})
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
document.getElementById("reg-btn").addEventListener("click",function() {
    const surname=document.getElementById("reg-surname").value.trim();
    const name=document.getElementById("reg-name").value.trim();
	const className=document.getElementById("reg-className").value.trim();
    if (surname===""||name===""||className==="") {
        message.textContent="Введите фамилию, имя и класс";
        return;
    }
    const token=localStorage.getItem("token");
    fetch("/api/register", {
        method:"POST",
        headers:{
            "Content-Type":"application/x-www-form-urlencoded",
            "Authorization":"Bearer "+token
        },
        body:"name="+encodeURIComponent(name)+"&surname="+encodeURIComponent(surname)+"&className="+encodeURIComponent(className)
    })
        .then(function(r) {return r.json();})
        .then(function(data) {
            if (data.result==="ok") {
                message.textContent="Ученик добавлен.";
                document.getElementById("reg-surname").value="";
                document.getElementById("reg-name").value="";
				document.getElementById("reg-className").value="";
            } else {
                message.textContent="Ошибка: "+data.message;
            }
        })
        .catch(function() { message.textContent = "Сервер недоступен"; });
});
document.getElementById("save-btn").addEventListener("click",function() {
    const token=localStorage.getItem("token");
    fetch("/api/save?",{headers:{"Authorization":"Bearer "+token}})
        .then(function(r) {return r.json();})
        .then(function(data) {
            message.textContent=data.result==="ok"?"Сохранено в файл.":"Ошибка: "+data.message;
        })
        .catch(function() {message.textContent="Сервер недоступен";});
});
function loadPendingAccounts() {
    fetch("/api/pending-accounts", {headers:{"Authorization":"Bearer "+localStorage.getItem("token")}})
        .then(function(r) {return r.json();})
        .then(function(data) {
            const box=document.getElementById("pending-accounts");
            box.textContent="";
            if (data.result!=="ok") {return;}
            if (data.rows.length===0) {box.textContent="Новых заявок нет";return;}
            data.rows.forEach(function(row) {
                const div=document.createElement("div");
                div.textContent=row.login+" ("+row.role+") ";
                const btn=document.createElement("button");
                btn.className="btn";
                btn.textContent="Подтвердить";
                btn.addEventListener("click",function() {approveAccount(row.login);});
                div.appendChild(btn);
                box.appendChild(div);
            });
        });
}
function approveAccount(login) {
    fetch("/api/approve-account",{
        method:"POST",
        headers:{"Content-Type": "application/x-www-form-urlencoded",
            "Authorization":"Bearer "+localStorage.getItem("token")},
        body:"login="+encodeURIComponent(login)
    })
    .then(function(r) {return r.json();})
    .then(function() {loadPendingAccounts();});
}
changePassButton.addEventListener("click",function() {
    location.href="change.html";
});
document.getElementById("students-btn").addEventListener("click",function() {
    location.href="student-list.html";
});
document.getElementById("summary-btn").addEventListener("click",function() {
	location.href="summary.html";
});
document.getElementById("to-reg-screen").addEventListener("click",function() {
	location.href="/reg-page.html";
});
showScreen();