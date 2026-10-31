if(!localStorage.getItem("token")||(localStorage.getItem("role")!=="CAFETERIA"&&localStorage.getItem("role")!=="MAIN_DEVELOPER")){
    location.href="/index.html";
}
const message={
    _timer:null,
    get textContent() {return messageBox.textContent;},
    set textContent(text) {
        messageBox.textContent=text;
        clearTimeout(this._timer);
        if(text!=="") {
            this._timer=setTimeout(function() {messageBox.textContent=""; },5000);
        }
    }
};
document.getElementById("back-btn").addEventListener("click",function(){
    location.href="/cafeteria.html";
});