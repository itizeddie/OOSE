/** Based on code from MDN web docs tutorial
 * https://developer.mozilla.org/en-US/docs/Mozilla/Add-ons/WebExtensions/Your_second_WebExtension
 */

/**
 * Listen for clicks on the buttons, and send the appropriate message to
 * the content script in the page.
 */
function listenForClicks() {


    document.addEventListener("click", (e) => {

        /**
         * Get page content and send a "add-course" message to the content script in the active tab.
         */
        function sendCourseInfoToContentScript(tabs) {
            browser.tabs.sendMessage(tabs[0].id, {
                command: "add-course"
            });
        }

        /**
         * Get username and password and send a "create-account" message to the content script in the active tab.
         */
        function sendSignupInfoToContentScript(tabs) {
            var username = document.getElementById("username").value;
            var email = document.getElementById("email").value;
            var password = document.getElementById("password").value;
            browser.tabs.sendMessage(tabs[0].id, {
                command: "create-account",
                username: username,
                email: email,
                password: password
            }).then(function() {
                document.querySelector("#login-content").classList.add("hidden");
                document.querySelector("#popup-content").classList.remove("hidden");
                document.querySelector("#login-content").insertAdjacentHTML("afterend", "<div id='signup-notification'>Sign up successful!</div>");
                setTimeout(function(){
                    document.getElementById("signup-notification").remove()
                }, 1000);

            });
        }

        /**
         * Log the error to the console.
         */
        function reportError(error) {
            console.error(`Could not add course: ${error}`);
        }

        /**
         * Get the active tab,
         * then call "sendCourseInfoToContentScript()".
         */
        var clickedItem = e.target.classList;

        if (clickedItem.contains("add-course")) {
            browser.tabs.query({active: true, currentWindow: true})
                .then(sendCourseInfoToContentScript)
                .catch(reportError);
        }
        if (clickedItem.contains("sign-up")) {
            browser.tabs.query({active: true, currentWindow: true})
                .then(sendSignupInfoToContentScript)
                .catch(reportError);
        }
    });
}

/**
 * There was an error executing the script.
 * Display the popup's error message, and hide the normal UI.
 */
function reportExecuteScriptError(error) {
    var elem = document.querySelectorAll("#popup-content, #login-content");
    elem.forEach(elem => {
        elem.classList.add("hidden");
    });
    document.querySelector("#error-content").classList.remove("hidden");
    console.error(`Failed to execute calendue content script: ${error.message}`);
}


/**
 * When the popup loads, inject a content script into the active tab,
 * and add a click handler.
 * If we couldn't inject the script, handle the error.
 */
browser.tabs.executeScript({file: "/content_scripts/calendue.js"})
    .then(listenForClicks)
    .catch(reportExecuteScriptError);