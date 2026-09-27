if(!localStorage.getItem("token")||localStorage.getItem("role")!=="ADMIN"){
    location.href="/index.html";
}