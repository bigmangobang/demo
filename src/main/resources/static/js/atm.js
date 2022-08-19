let socket;
let sid = $("#sid").val();
if (typeof (WebSocket) == "undefined") {
    console.log("你的浏览器不支持websocket");
}

socket = new WebSocket("ws://localhost:8080/webSocket/" + sid)
socket.onopen = function () {
    console.log("Socket 已打开");
    socket.send("消息发送测试(From Client)|" + sid);
};
//收到消息事件
socket.onmessage = function (msg) {
    const list = msg.data.split("|");
    let type = list[0];
    list.splice(0, 1);
    switch (type) {
        case   "init":
            addMember(list);
            break;
        case "member_join":
            addMember(list);
            break;
        case "member_quit":
            delMember(list);
            break;
        case "hero":
            console.log(msg.data);
            break;
    }
};
socket.onclose = function () {
    console.log("Socket已关闭");
};
//发生了错误事件
socket.onerror = function () {
    alert("Socket发生了错误");
}

//窗口关闭时，关闭连接
window.unload = function () {
    socket.close();
};

function addMember(list) {
    const ul = $("#atm_ul");
    let i = 1;
    for (const member of list) {

        let id = "right_" + 4;

        if (i === 1) {
            const $li = " <li id='" + member + "'><a href=\"#\" class=\"current\" >" + member + "</a></li>";
            $($li).appendTo(ul);
            continue;
        }
        if (i === list.length) {
            const $li = " <li id='" + member + "'><a href=\"#\" class=\"last\">" + member + "</a></li>";
            $($li).appendTo(ul);
            continue;
        }
        const $li = " <li id='" + member + "'><a href=\"#\" class=\"current\" >" + member + "</a></li>";
        $($li).appendTo(ul);
    }
}

function delMember(list) {
    const ul = $("#atm_ul");
    for (const member of list) {
        ul.remove( ul.getElementById(member));
    }
}

function startGame() {
    socket.send("start|" + sid);
}