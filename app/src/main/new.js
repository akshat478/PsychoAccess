const io = require("socket.io")(3000, {
    cors: { origin: "*" },
    allowEIO3: true
});

let users = []; // Temporary in-memory storage

io.on("connection", (socket) => {
    console.log("A user connected: " + socket.id);

    // 1. Handle Registration
    socket.on("register_user", (userData) => {
        userData.id = socket.id;
        users.push(userData);
        console.log("New user registered:", userData.username);
        // Notify admin to refresh their list
        io.emit("unverified_doctors_list", users.filter(u => u.role === 'DOCTOR' && !u.isVerified));
    });

    // 2. Admin: Get Unverified Doctors
    socket.on("get_unverified_doctors", () => {
        const unverified = users.filter(u => u.role === 'DOCTOR' && !u.isVerified);
        socket.emit("unverified_doctors_list", unverified);
    });

    // 3. Admin: Verify a Doctor
    socket.on("verify_doctor", (data) => {
        const user = users.find(u => u.username === data.username);
        if (user) {
            user.isVerified = true;
            console.log("Doctor verified:", data.username);
            socket.emit("verification_success");
            // Tell everyone to refresh their doctor lists
            const verified = users.filter(u => u.role === 'DOCTOR' && u.isVerified);
            io.emit("verified_doctors_list", verified);
        }
    });

    // 4. User: Get Verified Doctors
    socket.on("get_verified_doctors", () => {
        const verified = users.filter(u => u.role === 'DOCTOR' && u.isVerified);
        socket.emit("verified_doctors_list", verified);
    });

    // 5. Messaging
    socket.on("send_message", (msgData) => {
        console.log("Message from", msgData.sender, "to", msgData.receiver);
        io.emit("receive_message", msgData);
    });

    socket.on("disconnect", () => {
        console.log("User disconnected");
    });
});

console.log("Server running on port 3000");
