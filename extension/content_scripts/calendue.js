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

    /**
     * Sends the DOM the server using AJAX.
     * @param sendResponse  the function that returns a response from calendue.js to add_course.js
     */
    function sendDOMtoServer(sendResponse) {
        let response = "";
        let data = new FormData();
        data.append("token", create_UUID());
        data.append("url", window.location.href);
        data.append("document", document.documentElement.innerHTML);

        let xhr = new XMLHttpRequest();
        xhr.withCredentials = true;

        xhr.onload = function(){
            if (xhr.readyState === XMLHttpRequest.DONE && xhr.status === 200) {
                response = "Course assignment successfully added!";
            } else {
                response = "Could not send to server. Error: " + this.status + ". " + this.responseText;
            }
            sendResponse({ result: response });
        };

        xhr.open("POST", "http://calendue.herokuapp.com/scrape");
        xhr.send(data);
    }

    /**
     * Sends request to server to create an account.
     * @param message       contains username, password, and email
     * @param sendResponse  the function that returns a response from calendue.js to add_course.js
     */
    function createAccount(message, sendResponse) {
        let response = "";
        let data = new FormData();
        data.append("username", message.username);
        data.append("password", message.password);
        data.append("email", message.email);

        let xhr = new XMLHttpRequest();
        xhr.withCredentials = true;

        xhr.onload = function(){
            if(xhr.readyState === XMLHttpRequest.DONE && xhr.status === 201) {
                response = "Successfully created account!";
                loginToServer(message, sendResponse);
            } else {
                response = "Could not create account. Error: " + this.status + ". " + this.responseText;
                sendResponse({ result: response });
            }

        };

        xhr.open("POST", "http://calendue.herokuapp.com/accounts");
        xhr.send(data);
    }
    
    function loginToServer(message, sendResponse) {
        let response = "";
        let data = new FormData();
        data.append("username", message.username);
        data.append("password", message.password);

        let xhr = new XMLHttpRequest();
        xhr.withCredentials = true;

        xhr.onload = function(){
            if (xhr.readyState === XMLHttpRequest.DONE && xhr.status === 200) {
                response = "Successfully logged in!";
            } else {
                response = "Could not login. Error: " + this.status + ". Invalid username or password.";
            }
            sendResponse({ result: response });
        };

        xhr.open("POST", "http://calendue.herokuapp.com/login");
        xhr.send(data);
    }

    function logoutFromServer(sendResponse) {
        let response = "";
        let xhr = new XMLHttpRequest();
        xhr.withCredentials = true;

        xhr.onload = function(){
            if (xhr.readyState === XMLHttpRequest.DONE && xhr.status === 200) {
                response = "Successfully logged out!";
            } else {
                response = "Could not logout. Error: " + this.status + ".";
            }
            sendResponse({ result: response });
        };

        xhr.open("GET", "http://calendue.herokuapp.com/logout");
        xhr.send();
    }

    /**
     * Creates UUID. Function taken from
     * https://www.w3resource.com/javascript-exercises/javascript-math-exercise-23.php
     * @returns {string}
     */
    function create_UUID(){
        let dt = new Date().getTime();
        return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
            let r = (dt + Math.random()*16)%16 | 0;
            dt = Math.floor(dt/16);
            return (c==='x' ? r :(r&0x3|0x8)).toString(16);
        });
    }

    /**
     * Listen for messages from the background script.
     */
    browser.runtime.onMessage.addListener((message, sender, sendResponse) => {
        if (message.command === "add-course") {
            sendDOMtoServer(sendResponse);
        } else if (message.command === "create-account") {
            createAccount(message, sendResponse);
        } else if (message.command === "login") {
            loginToServer(message, sendResponse);
        } else if (message.command === "logout") {
            logoutFromServer(sendResponse);
        }
        return true;
    });
})();