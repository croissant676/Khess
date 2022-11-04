let low_x = 0;
let low_y = 0;

let high_x = 8;
let high_y = 8;

let current_state = 0;
// 0 -> login
// 1 -> game
// 2 -> game over
function change_state(state) {
}

let prev_state = current_state;

function check_state() {
    if (current_state !== prev_state) {
        // change state
        
    }
    
    if (current_state === 0) {
        display_login();
    } else if (current_state === 1) {
        
    }
}

function display_login() {
    let login = document.getElementById("login");
    // create a login page
    let login_form = document.createElement("form");
    login_form.setAttribute("id", "login_form");
    login_form.setAttribute("method", "post");
    login_form.setAttribute("action", "javascript:login();");
    login.appendChild(login_form);
    
}

class position {
    constructor(x, y) {
        this.x = x;
        this.y = y;
    }
}

class piece {
    constructor(player, position, type) {
        this.player = player;
        this.position = position;
        this.type = type;
    }
}

class board_view {
    constructor() {
        this.board = new Array(high_x - low_x + 1);
        for (let i = 0; i < high_x - low_x + 1; i++) {
            this.board[i] = new Array(high_y - low_y + 1);
        }
        this.board_div = document.getElementById("board");
    }
    
    display_board() {
        for (let i = 0; i < high_x - low_x + 1; i++) {
            let c_row = document.createElement("div");
            c_row.classList.add("row");
            c_row.id = "row" + i;
            // at the beginning of the row, add the row number
            let c_row_number = document.createElement("p");
            c_row_number.classList.add("row_header");
            c_row_number.innerHTML = "" + i;
            c_row.appendChild(c_row_number);
            
            for (let j = 0; j < high_y - low_y + 1; j++) {
                let c_div = document.createElement("div");
                c_div.classList.add("cell");
                if ((i + j) % 2 === 0) {
                    c_div.classList.add("white");
                } else {
                    c_div.classList.add("black");
                }
                c_div.id = "cell_" + i + "_" + j;
                c_row.appendChild(c_div);
            }
            this.board_div.appendChild(c_row);
        }
    }
}

class server_connection {
    // websockets
    constructor() {
        this.ws = new WebSocket("ws://localhost:8080");
        this.ws.onopen = function() {
            console.log("Connected to server");
        }
        this.ws.onmessage = function(event) {
            console.log(event.data);
        }
        this.ws.onclose = function() {
            console.log("Disconnected from server");
        }
    }
}

window.onload = function() {
    check_state();
}