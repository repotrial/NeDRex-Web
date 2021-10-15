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


  getNodes(type,ids,attrs){
    return this.post("/mapIdListToItems",{type:type, list:ids, attributes: attrs}).then(response=>{
      return response.data;
    }).catch(console.error)
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

  },
  getTrials(disorders, drugs,lower,upper) {
    let disorderString = disorders.reduce((d1,d2)=>d1+"+OR+"+d2).replaceAll(" ","+")
    let drugString = drugs.reduce((d1,d2)=>d1+"+OR+"+d2).replaceAll(" ","+")
    return this.get("https://clinicaltrials.gov/api/query/study_fields?expr=("+disorderString+")+AND+("+drugString+")&min_rnk="+lower+"&max_rnk="+upper+"&fields=NCTId,InterventionName,Condition&fmt=json").then(response=>{
      return response.data["StudyFieldsResponse"]
    }).catch(console.error)
  },

  getAllTrials(disorders, drugs){
    return this.getTrials(disorders,drugs,1,1000).then(data => {
      let total = data.NStudiesFound
      for (let i = 1; i * 1000 < total; i += 1) {
        this.getTrials(disorders, drugs, i * 1000+1, (i + 1) * 1000).then(data2 => {
          data.StudyFields = data.StudyFields.concat(data2.StudyFields)
        })
      }
      return data
    }).catch(console.error)
  }
}

export default ApiService
