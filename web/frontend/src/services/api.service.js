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


  getNodes(type, ids, attrs) {
    return this.post("/mapIdListToItems", {type: type, list: ids, attributes: attrs}).then(response => {
      return response.data;
    }).catch(console.error)
  },

  getJobByGraph(gid){
    return this.get("/getJobByGraph?gid="+gid).then(response=>{
      return response.data
    }).catch(console.error)
  },
  getJob(jid) {
    return this.get("/getJob?id="+jid).then(response=>{
      return response.data
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
    return this.post("/getInteractingOnly", {type: type, ids: nodeIDs}).then(response => {
      return response.data
    })

  },
  getTrials(disorders, drugs, lower, upper) {
    return this.get("https://clinicaltrials.gov/api/query/study_fields?expr=(" + disorders + ")+AND+(" + drugs + ")&min_rnk=" + lower + "&max_rnk=" + upper + "&fields=NCTId,InterventionName,Condition&fmt=json").then(response => {
      return response.data["StudyFieldsResponse"]
    }).catch(console.error)
  },

  createTrialRequestStrings(list) {
    let requests = [""]
    let current = 0
    list.forEach(d => {
      let cl = requests[current].length;
      if (cl + 4 + d.length > 500) {
        requests.push("")
        cl = 0
        current++
      }
      if (cl !== 0)
        requests[current] += "+OR+"
      requests[current] += d
    })
    for (let i = 0; i < requests.length; i++) {
      requests[i] = requests[i].replaceAll(" ", "+")
    }
    return requests;
  },

  async getAllTrials(disorders, drugs) {
    let disorderStrings = this.createTrialRequestStrings(disorders)
    let drugStrings = this.createTrialRequestStrings(drugs)
    let data = undefined
    for (const disorderString of disorderStrings) {
      for (const drugString of drugStrings) {
        await this.getTrials(disorderString, drugString, 1, 1000).then(async data => {
          let total = data.NStudiesFound
          for (let i = 1; i * 1000 < total; i += 1) {
            await this.getTrials(disorderString, drugString, i * 1000 + 1, (i + 1) * 1000).then(data2 => {
              data.StudyFields = data.StudyFields.concat(data2.StudyFields)
            })
          }
          return data
        }).then(resp => {
          if (data === undefined)
            data = resp
          else
            data.StudyFields = data.StudyFields != null ? data.StudyFields.concat(resp.StudyFields) : []
        }).catch(console.error)
      }
    }
    return data
  },
  getDisorderHierarchy(sid) {
    return this.get("/getDisorderHierarchy?sid=" + sid).then(response => {
      return response.data
    })
  },
  getLayout(gid, type) {
    return this.get("/getLayout?gid=" + gid + "&type=" + type).then(response => {
      return response.data
    })
  },


  getExampleInputFile(type){
    return this.get("/getExampleInputFileLink?type="+type).then(response=>{
      return response.data;
    })
  },

  getLicense() {
    return this.get("getLicense").then(response => {
      if (response.data != null) {
        return response.data
      }
    }).then(text => {
      let eula = []
      let lastHeader = undefined
      let lastContent = undefined
      let bulletPointIndent = false;
      let bulletPointDoubleIntent = false;
      text.split("\n").forEach(line => {
        if (line.indexOf("====") > -1) {
          if (lastHeader != null) {
            if(bulletPointDoubleIntent)
              lastContent = lastContent+"</div>"
            if(bulletPointIndent)
              lastContent = lastContent+"</div>"
            bulletPointDoubleIntent=false;
            eula.push({title: lastHeader, content: this.formatEULA(lastContent)})
          }
          lastHeader = line.replaceAll("=", "").trim()
          lastContent = ""
        } else {
          line = line.trim()
          if (new RegExp("^[0-9]","i").test(line)) {
            if (bulletPointDoubleIntent)
              lastContent += "</div>"
            if(bulletPointIndent)
              lastContent+="</div>"
            bulletPointIndent=true

            bulletPointDoubleIntent=new RegExp("^[0-9]\.[0-9]","i").test(line);
            if(bulletPointDoubleIntent){
              line="<div style=\"margin-left:50px\">"+line
            }else{
              line = "<div style=\"margin-left:25px\">"+line
            }
          }
          lastContent += line + "<br>"
        }
      })
      if (lastHeader != null) {
        eula.push({title: lastHeader, content: this.formatEULA(lastContent)})
      }
      eula.push({title:"Redistribution",content:"It is prohibited to distribute downloaded data containing DrugBank drug-target information to any third party!"})
      return eula
    })

  },

  formatEULA: function (text) {
    if (text.indexOf("https:\/\/omim\.org\/downloads"))
      text = text.replace("https:\/\/omim\.org\/downloads", "<a href='https://omim.org/downloads' target='_blank'>https://omim.org/downloads")
    text = text.replaceAll("OMIM��","OMIM®")
    text = text.replaceAll("OMIM Data��","OMIM®")
    text = text.replaceAll("Online Mendelian Inheritance in Man��","Online Mendelian Inheritance in Man®")
    text = text.replaceAll("JHU���", "JHU'")
    text = text.replaceAll("���claims","‘claims")
    text = text.replaceAll("made���","made’")
    text = text.replaceAll("UH���s","UH's")
    text = text.replaceAll("Copyright ��", "Copyright ©")
    text = text.replaceAll("���JHU Parties���",'“JHU Parties”')
    text = text.replaceAll("���prior acts���","'prior acts'")
    return text
  },
  getInteractionEdges: function(data) {
    return this.post("/getInteractionEdges",data).then(response=>{
      if(response.data!=null)
        return response.data
    }).catch(console.error)
  },

}

export default ApiService
