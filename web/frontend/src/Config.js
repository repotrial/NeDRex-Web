const HOST = (process.env.NODE_ENV === "development" ? (process.env.FRONTEND_HOST_URL ? process.env.FRONTEND_HOST_URL : "localhost") : "localhost")
export const BACKEND_PORT = (process.env.NODE_ENV === "development" ? "8080" : "8090")
export const HOST_URL = "http://" + HOST + ":" + BACKEND_PORT
export const SOCKET_URL = "ws://" + HOST + ":8090"
