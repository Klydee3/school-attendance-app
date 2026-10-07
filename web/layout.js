function two(n) {return n<10?"0"+n:""+n;}
(function() {
    const token=localStorage.getItem("token");
    const role=localStorage.getItem("role");
    const now=new Date();
    const months=["января","февраля","марта","апреля","мая","июня",
                    "июля","августа","сентября","октября","ноября","декабря"];
    const hour=now.getHours();
    const greet=hour<6?"Доброй ночи!"
              :hour<12?"Доброе утро!"
              :hour<18?"Добрый день!":"Добрый вечер!";
    const header=document.createElement("div");
    header.id="greet-header";
	setInterval(function() {
		const n=new Date();
		const t=document.getElementById("greet-time");
		if (t) {
			t.textContent=two(n.getHours())+":"+two(n.getMinutes());
		}
	}, 1000);
    header.innerHTML=
    '<span class="greet-date">'+now.getDate()+" "+
    months[now.getMonth()]+" "+now.getFullYear()+
    ', <span id="greet-time">'+two(hour)+":"+two(now.getMinutes())+"</span></span>"+
    '<span class="greet-text">'+greet+"</span>";
    const rail=document.createElement("div");
    rail.id="side-rail";
    rail.innerHTML=
        '<button class="side-icon" id="rail-tools" title="Инструменты">☰</button>'+
        '<button class="side-icon" id="rail-person" title="Кабинет">☺</button>'+
        '<button class="side-icon" id="rail-gear" title="Настройки">⚙</button>';
    const panel=document.createElement("div");
    panel.id="side-panel";
    let links="";
    if (!token){
        links+='<a href="/index.html">Вход</a>';
        links+='<a href="/reg-page.html">Регистрация</a>';
    }else{
        links+='<a href="/index.html">Кабинет</a>';
        if (role === "ADMIN") {
			links += '<a href="/student-list.html">Список учеников</a>';
			links += '<a href="/register-student.html">Регистрация ученика</a>';
			links += '<a href="/applications.html">Заявки на аккаунты</a>';
		}
        if (role==="ADMIN"||role==="TEACHER"||role==="CAFETERIA") {
            links+='<a href="/summary.html">Утренняя сводка</a>';
        }
        links+='<a href="/change.html">Сменить пароль</a>';
        links+='<a href="#" id="side-logout">Выйти</a>';
    }
    panel.innerHTML=links;
    document.body.insertBefore(header,document.body.firstChild);
    document.body.insertBefore(rail,document.body.firstChild);
    document.body.insertBefore(panel,document.body.firstChild);
    function closePanel() {
        panel.classList.remove("open");
    }
    document.getElementById("rail-tools").addEventListener("click",function() {
        panel.classList.toggle("open");
    });
    document.getElementById("rail-person").addEventListener("click",function() {
        closePanel();
        location.href="/index.html";
    });
    document.getElementById("rail-gear").addEventListener("click",function() {
        closePanel();
        location.href=token?"/change.html":"/index.html";
    });
    document.addEventListener("click",function(e) {
        if (!panel.classList.contains("open")) {
            return;
        }
        if (panel.contains(e.target)||rail.contains(e.target)) {
            return;
        }
        closePanel();
    });
    const logout=document.getElementById("side-logout");
    if (logout) {
        logout.addEventListener("click",function(e) {
            e.preventDefault();
            localStorage.removeItem("token");
            localStorage.removeItem("role");
            location.href="/index.html";
        });
    }
})();