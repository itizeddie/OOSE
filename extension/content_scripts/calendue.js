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

    //function checkURL () {
        if(window.location.hostname!=="www.gradescope.com"){
            console.log("notongradescope function");
            let t = document.createTextNode("Not on gradescope");
            document.body.appendChild(t);
        }
        else{
            console.log("on");
        }

   // }

    /**
     * Sends the account data to the server to create the account using a POST request.
     * @param username  the provided username of the user
     * @param password  the provided password of the user
     * @param email     the provided email of the user
     */
    function createAccount(username, password, email) {
        var data = new FormData();
        data.append("username", username);
        data.append("password", password);
        data.append("email", email);

        var xhr = new XMLHttpRequest();
        xhr.withCredentials = true;
        xhr.onreadystatechange = function(){
            if(xhr.readyState === XMLHttpRequest.DONE && xhr.status === 200) {
                console.log(this.responseText);
            }
        };

        xhr.open("POST", "http://localhost:7000/accounts");
        xhr.send(data);
    }


    /**
     * Listen for messages from the background script.
     */
    browser.runtime.onMessage.addListener((message) => {
        console.log(message.command);
        if (message.command === "add-course") {
            var content = document.documentElement.innerHTML;
            console.log(content);
        } else if (message.command === "create-account") {
            createAccount(message.username, message.password, message.email);
            console.log(message.username+" "+message.password+" "+message.email);
        }
    });

})();