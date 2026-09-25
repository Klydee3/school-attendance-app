const messageBox=document.getElementById("message");
const message={
    _timer:null,
    get textContent(){return messageBox.textContent;},
    set textContent(text) {
        messageBox.textContent=text;
        clearTimeout(this._timer);
        if (text!==""){
            this._timer=setTimeout(function() {messageBox.textContent="";},2500);
        }
    }
};
const token=localStorage.getItem("token");
const role=localStorage.getItem("role");
if (!token||role==="STUDENT") {
    location.href="/index.html";
}
function two(n) {return n<10?"0"+n:""+n;}
const now=new Date();
const dateInput=document.getElementById("summary-date");
dateInput.value=now.getFullYear()+"-"+two(now.getMonth()+1)+"-"+two(now.getDate());
let lastData=null;
function loadSummary() {
    fetch("/api/summary?date="+dateInput.value, {
        headers:{"Authorization":"Bearer "+token}
    })
        .then(function(r) {return r.json();})
        .then(function(data) {
            if (data.result!=="ok") {
                message.textContent="Ошибка: "+data.message;
                return;
            }
            lastData=data;
            const tbody=document.querySelector("#summary-table tbody");
            tbody.textContent="";
            data.rows.forEach(function(row) {
                const tr=document.createElement("tr");
                tr.innerHTML="<td>"+row["class"]+"</td><td>"+row.total+
                    "</td><td>"+row.present+"</td><td>"+row.absent+
                    "</td><td>"+row.percent+"%</td>";
                tbody.appendChild(tr);
            });
            document.getElementById("summary-total").textContent=
                "По школе: пришло "+data.presentAll+" из "+data.totalAll;
        })
        .catch(function() {message.textContent="Сервер недоступен";});
}
document.getElementById("show-btn").addEventListener("click",loadSummary);
document.getElementById("csv-btn").addEventListener("click",function() {
    if (!lastData) {
        message.textContent="Сначала покажи сводку";
        return;
    }
    let csv="Класс;Всего;Пришло;Не пришло;Процент\n";
    lastData.rows.forEach(function(row) {
        csv+=row["class"]+";"+row.total+";"+row.present+";"+
            row.absent+";"+row.percent+"\n";
    });
    const percentAll=lastData.totalAll>0
        ?Math.round(lastData.presentAll*100/lastData.totalAll):0;
    csv+="По школе;"+lastData.totalAll+";"+lastData.presentAll+";"+
        (lastData.totalAll-lastData.presentAll)+";"+percentAll+"\n";
    const blob=new Blob(["\uFEFF"+csv],{type:"text/csv;charset=utf-8"});
    const a=document.createElement("a");
    a.href=URL.createObjectURL(blob);
    a.download="summary-"+lastData.date+".csv";
    a.click();
});
document.getElementById("back-btn").addEventListener("click",function() {
    location.href="/index.html";
});
loadSummary();