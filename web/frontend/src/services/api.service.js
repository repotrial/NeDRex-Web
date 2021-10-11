import axios from 'axios'

const ApiService = {
  setNedrex(url) {
    axios.nedrex = url
  },

  init(baseURL) {
    axios.defaults.baseURL = baseURL;
  },

  get(resource) {
    return axios.get(resource)
  },

  post(resource, data, callback) {
    return axios.post(resource, data, callback);
  },

  put(resource, data) {
    return axios.put(resource, data)
  },

  delete(resource) {
    return axios.delete(resource)
  },

  isReady() {
    return this.get("/isReady").then(response => {
      return response.data
    })
  },

  getGraph(gid) {
    return this.get("/getGraph?id=" + gid).then(response => {
      if (response.data)
        return response.data
    })
  },

  getMetagraph() {
    return this.get("/getMetagraph").then(response => {
      return response.data
    })
  },

  getMetadata() {
    return this.get("getMetadata").then(response => {
      return response.data
    })
  }
  ,

  requestGraph(payload) {
    return this.post("/getGraphInfo", payload)
  },

  removeGraph(id) {
    this.get("/removeGraph?gid=" + id).catch(console.error)
  },

  saveGraph(gid, uid) {
    return this.get("/archiveHistory?uid=" + uid + "&gid=" + gid)
  },

  postNedrex(route, data) {
    return this.post("/postNedrex", {route: route, data: data})
  },

  getValidationScore(id) {
    return this.getNedrex("/validation/status?uid=" + id)
  },

  getNedrex(route) {
    return this.post("/getNedrex", {route: route})
  },

  validateModule(data) {
    return this.postNedrex("/validation/module", data)
  },
  validateDrugs(data) {
    return this.postNedrex("/validation/drug", data)
  },

  /**
   * Perform a custom Axios request.
   *
   * data is an object containing the following properties:
   *  - method
   *  - url
   *  - data ... request payload
   *  - auth (optional)
   *    - username
   *    - password
   **/
  customRequest(data) {
    return axios(data)
  },

  getTableDownload(gid, type, name, attributes) {
    return this.post("/getTableDownload", {gid: gid, type: type, name: name, attributes: attributes}).then(response => {
      return response.data
    })
  },
  getQuickExample(nr, type) {
    return this.get("/getQuickExample?nr=" + nr + "&nodeType=" + type).then(response => {
      return response.data
    })
  },
  getInteractingOnly(type, nodeIDs) {
    return this.post("/getInteractingOnly",{type:type, ids:nodeIDs}).then(response=>{
      return response.data
    })

  }
}

export default ApiService
