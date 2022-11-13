let enter_button = document.getElementById("enter");
let enter_div = document.getElementById("enter-div");
let initialized = false;
let ws = null;

function send_request() {
    let username = document.getElementById("username-input").value;
    console.assert(ws != null);
    ws.send(JSON.stringify({
        type: "create",
        username: username
    }));
}


enter_button.onclick = function () {
    try {
        ws = new WebSocket('ws://localhost:8080/game');
    } catch (e) {
        console.log(e);
        alert("Failed to connect to server. :(");
        return;
    }
    ws.onopen = function () {
        send_request();
        initialize_board();
    };
}

// api stuff

class position {
    constructor(x, y) {
        this.x = x;
        this.y = y;
    }
}

class tile {
    constructor(position) {
        this.position = position;
        this.x = position.x;
        this.y = position.y;
        this.piece = null;
    }

    is_even() {
        return (this.x + this.y) % 2 === 0;
    }
}

class piece {
    constructor(owner) {

    }
}

class player {
    constructor(name, color, id) {
        this.name = name;
        this.color = color;
        this.id = id;
    }
}

let game_div = document.getElementById("game-div");
game_div.style.display = "none";
let board = document.getElementById("board");
let player_stats = document.getElementById("player-stats");
let lb = document.getElementById("lb");

let board_size;
let minor_side;
let major_side;
let active_player_count;

let top_corner, bottom_corner;
const tile_len = () => bottom_corner.x - top_corner.x + 1;
const tile_size = () => {
    return {x: board.clientWidth / tile_len(), y: board.clientHeight / tile_len()}
};

function initialize_board() {
    enter_div.style.display = "none";
    game_div.style.display = "block";
    ws.onmessage = function (event) {
        console.log(event);
        if (typeof event.data !== "string") {
            return;
        }
        let data = JSON.parse(event.data);
        switch (data.type) {
            case "game_info":
                board_size = data.board_size;
                minor_side = data.minor_side;
                major_side = data.major_side;
                active_player_count = data.active_player_count;
                break;
            case "viewframe":

                break;
        }
    }
}

function display_board() {

}