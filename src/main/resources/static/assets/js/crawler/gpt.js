const userInput = document.getElementById("userInput");
const sendButton = document.getElementById("sendButton");
const messagesContainer = document.getElementById("messages");

function appendMessage(content, sender) {
    const messageDiv = document.createElement("div");
    messageDiv.classList.add("message", sender);
    messageDiv.textContent = content;
    messagesContainer.appendChild(messageDiv);
    messagesContainer.scrollTop = messagesContainer.scrollHeight;
}

async function sendMessage() {
    const userMessage = userInput.value.trim();
    if (!userMessage) return;

    appendMessage(userMessage, "user");
    userInput.value = "";

    try {
        const response = await fetch("/api/chat", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify({ prompt: userMessage }),
        });

        if (!response.ok) {
            throw new Error("Failed to get response from the server.");
        }

        const data = await response.json();
        const botMessage = data.content;
        appendMessage(botMessage, "bot");
    } catch (error) {
        console.error("Error:", error);
        appendMessage("An error occurred. Please try again.", "bot");
    }
}

sendButton.addEventListener("click", sendMessage);

userInput.addEventListener("keypress", (e) => {
    if (e.key === "Enter") {
        sendMessage();
    }
});
