/** Based on code from MDN web docs tutorial
 * https://developer.mozilla.org/en-US/docs/Mozilla/Add-ons/WebExtensions/Your_second_WebExtension
 */

(function() {
    /**
     * Check and set a global guard variable.
     * If this content script is injected into the same page again,
     * it will do nothing next time.
     */
    if (window.hasRun) {
        return;
    }
    window.hasRun = true;

    console.log("hostname= "+ window.location.hostname);

    /**
     * Sends a post request with data to the server using AJAX.
     *
     * @param url       the url the post request is sent to
     * @param data      the data that is being sent
     */
    function sendDOMtoServer(content, url, token, sendResponse) {
        var response = "";
        var data = new FormData();
        data.append("token", token);
        data.append("url", url);
        data.append("document", document);

        var xhr = new XMLHttpRequest();
        xhr.withCredentials = true;

        xhr.onload = function(){
            if (xhr.readyState === XMLHttpRequest.DONE && xhr.status === 200) {
                response = "Course assignment successfully added!";
            } else {
                response = "Could not send to server. Error: " + this.status + ". " + this.responseText;
            }
            sendResponse({ result: response });
        };

        xhr.open("POST", "http://localhost:7000/scrape");
        xhr.send(data);
    }

    /**
     * Creates UUID. Function taken from
     * https://www.w3resource.com/javascript-exercises/javascript-math-exercise-23.php
     * @returns {string}
     */
    function create_UUID(){
        var dt = new Date().getTime();
        var uuid = 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
            var r = (dt + Math.random()*16)%16 | 0;
            dt = Math.floor(dt/16);
            return (c=='x' ? r :(r&0x3|0x8)).toString(16);
        });
        return uuid;
    }

    function createAccount(username, password, email, sendResponse) {
        var response = "";
        var data = new FormData();
        data.append("username", username);
        data.append("password", password);
        data.append("email", email);

        var xhr = new XMLHttpRequest();
        xhr.withCredentials = true;

        xhr.onload = function(){
            if(xhr.readyState === XMLHttpRequest.DONE && xhr.status === 201) {
                response = "Successfully created account! Please click the extension again to add assignment.";
                loginToServer(username, password, sendResponse);
            } else {
                response = "Could not create account. Error: " + this.status + ". " + this.responseText;
            }
            sendResponse({ result: response });
        };

        xhr.open("POST", "http://localhost:7000/accounts");
        xhr.send(data);
    }

    function loginToServer(username, password, sendResponse) {
        var response = "";
        var data = new FormData();
        data.append("username", username);
        data.append("password", password);

        var xhr = new XMLHttpRequest();
        xhr.withCredentials = true;

        xhr.onload = function(){
            if (xhr.readyState === XMLHttpRequest.DONE && xhr.status === 200) {
                console.log("Updated response after logging in");
                response = "Successfully logged in! Please click the extension again to add assignment.";
            } else {
                response = "Could not login. Error: " + this.status + ". Invalid username or password.";
            }
            sendResponse({ result: response });
        };

        xhr.open("POST", "http://localhost:7000/login");
        xhr.send(data);
    }

    /**
     * Listen for messages from the background script.
     */
    browser.runtime.onMessage.addListener((message, sender, sendResponse) => {
        console.log(message.command);
        if (message.command === "add-course") {
            var content = document.documentElement.innerHTML;
            var url = window.location.href;
            var token = create_UUID();
            sendDOMtoServer(content, url, token, sendResponse);
        } else if (message.command === "create-account") {
            createAccount(message.username, message.password, message.email, sendResponse);
            console.log(message.username+" "+message.password+" "+message.email);
        } else if (message.command === "login") {
            loginToServer(message.username, message.password, sendResponse);
        }
        return true;
    });
})();