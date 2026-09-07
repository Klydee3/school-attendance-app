const message = document.getElementById("message");
const loginScreen = document.getElementById("login-screen");
const studentScreen = document.getElementById("student-screen");
const teacherScreen = document.getElementById("teacher-screen");
const logoutBtn = document.getElementById("logout-btn");
function showScreen() {
    const role = localStorage.getItem("role");
    loginScreen.style.display = "none";
    studentScreen.style.display = "none";
    teacherScreen.style.display = "none";
    logoutBtn.style.display = "none";
    if (role === "STUDENT") {
        studentScreen.style.display = "block";
        logoutBtn.style.display = "block";
    } else if (role === "TEACHER" || role === "ADMIN") {
        teacherScreen.style.display = "block";
        logoutBtn.style.display = "block";
    } else {
        loginScreen.style.display = "block";
    }
}
document.getElementById("login-btn").addEventListener("click", function() {
    const login = document.getElementById("login-input").value.trim();
    const password = document.getElementById("password-input").value.trim();
    if (login === "" || password === "") {
        message.textContent = "Введите логин и пароль";
        return;
    }
    fetch("/login?login=" + encodeURIComponent(login) + "&password=" + encodeURIComponent(password))
        .then(function(r) { return r.json(); })
        .then(function(data) {
            if (data.result === "ok") {
                localStorage.setItem("token", data.token);
                localStorage.setItem("role", data.role);
                message.textContent = "";
                showScreen();
            } else {
                message.textContent = "Ошибка: " + data.message;
            }
        })
        .catch(function() { message.textContent = "Сервер недоступен"; });
});
function attend(answer) {
    const token = localStorage.getItem("token");
    fetch("/attend?token=" + encodeURIComponent(token) + "&answer=" + answer)
        .then(function(r) { return r.json(); })
        .then(function(data) {
            if (data.result === "ok") {
                message.textContent = answer === "yes"
                    ? "Спасибо! Ждите подтверждения учителя."
                    : "Принято. Порция не нужна.";
            } else {
                message.textContent = "Ошибка: " + data.message;
            }
        })
        .catch(function() { message.textContent = "Сервер недоступен"; });
}
document.getElementById("btn-yes").addEventListener("click", function() { attend("yes"); });
document.getElementById("btn-no").addEventListener("click", function() { attend("no"); });
logoutBtn.addEventListener("click", function() {
    localStorage.removeItem("token");
    localStorage.removeItem("role");
    showScreen();
});
showScreen();