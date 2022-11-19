let enter_button = document.getElementById("enter");
let enter_div = document.getElementById("enter-div");
let initialized = false;
let ws = null;

let current_player_name = null;

function send_request() {
    let username = document.getElementById("username").value;
    send(JSON.stringify({
        type: "create",
        username: username
    }));
    current_player_name = username;
}

function send(data) {
    if (ws === null) {
        console.error("WebSocket is not initialized.");
    }
    if (ws.readyState === WebSocket.OPEN) {
        console.debug("Sending data: " + data);
        ws.send(data);
    } else {
        console.error("WebSocket is not open.");
    }
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
    constructor(player) {
        this.player = player;
        this.type = null;
        this.position = null;
    }

    move(tile) {
        this.position = tile;
        tile.piece = this;
    }
}

class pawn extends piece {
    constructor(player) {
        super(player);
        this.direction = null;
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
let side_length;
const tile_size = () => {
    return {x: board.clientWidth / side_length, y: board.clientHeight / side_length}
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
                top_corner = new position(data.top_corner.x, data.top_corner.y);
                bottom_corner = new position(data.bottom_corner.x, data.bottom_corner.y);
                side_length = data.side_len;

                break;
        }

    }
}

function update_board_state() {

}

function display_board() {
    while (board.firstChild) {
        board.removeChild(board.firstChild);
    }
    for (let i = 0; i < board_size; i++) {
        let row = document.createElement("div");
        row.classList.add("row");
        board.appendChild(row);
        for (let j = 0; j < board_size; j++) {
            let tile = document.createElement("div");
            tile.classList.add("tile");
            row.appendChild(tile);
        }
    }
}

function update_everything() {

}
