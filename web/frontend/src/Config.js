
export const HOST = process.env.NODE_ENV === "development" ?"localhost": "localhost"
export const BACKEND_PORT = process.env.NODE_ENV === "development" ? "8080" : "8090"
export const HOST_URL =  "http://"+ (process.env.NODE_ENV ==="development" ? "localhost:8080" : "localhost:8090")
export const SOCKET_URL = "ws://"+(process.env.NODE_ENV ==="development" ? "localhost:8090" : "localhost:8090")
