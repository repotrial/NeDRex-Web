<template>
  <v-card ref="jobs" elevation="3" style="margin:15px">
    <v-list-item @click="show=!show">
      <v-list-item-title>
        <v-icon left>{{ show ? "far fa-minus-square" : "far fa-plus-square" }}</v-icon>
        Jobs
      </v-list-item-title>
    </v-list-item>
    <v-divider></v-divider>
    <template v-if="show">
      <v-tabs
        fixed-tabs
        v-model="jobsTabModel"
      >
        <v-tabs-slider></v-tabs-slider>
        <v-tab v-for="tab in ['Current Graph','All']" class="primary--text" :key=tab>
          {{ tab }}
        </v-tab>
      </v-tabs>

      <v-simple-table>
        <template v-slot:default>
          <thead>
          <tr>
            <th class="text-left">
              Status
            </th>
            <th class="text-left">
              Result
            </th>
            <th>
              Timestamp
            </th>
            <th class="text-left">
              Tool
            </th>

          </tr>
          </thead>
          <tbody>
          <tr
            v-for="job in (jobsTabModel===0? graphjobs:jobs)" :key="job.jid"
          >
            <td>
              <v-chip :color="job.state==='DONE'?(job.gid===gid?'blue':'green'):(job.state ==='ERROR' || job.state==='TIMEOUT' || job.state==='LIMITED' ?'red':'orange')"
                      :disabled="job.state!=='DONE'||job.gid===gid"
                      @click="$emit('graphLoadEvent', {post: {id: job.gid}})">

                <v-icon left v-if="job.state==='DONE'">
                  fas fa-check
                </v-icon>
                <v-icon letf v-else-if="job.state ==='ERROR' || job.state==='TIMEOUT' || job.state==='LIMITED'">
                  fas fa-exclamation-triangle
                </v-icon>
                <v-icon left v-else>
                  fas fa-circle-notch fa-spin
                </v-icon>
                [{{ job.state }}]
              </v-chip>
            </td>
            <td>
              <template v-if="job.state==='DONE'">
                <v-tooltip left>
                  <template v-slot:activator="{ on, attrs }">
                    <v-icon v-if="job.download" v-bind="attrs" v-on="on" right
                            @click="downloadJob(job)"
                    >
                      fas fa-download
                    </v-icon>
                    <v-icon v-else v-bind="attrs" v-on="on" right
                            @click="$emit('printNotificationEvent', 'No Download Available', 1)">fas fa-dna
                    </v-icon>
                  </template>
                  <span> added {{ job.update }} {{ job.target }} nodes</span>
                </v-tooltip>
                +{{ job.update }}
              </template>
            </td>
            <td>
              <v-tooltip left>
                <template v-slot:activator="{ on, attrs }">
                  <v-icon v-bind="attrs" v-on="on" right>
                    fas fa-history
                  </v-icon>
                </template>
                <span>{{ formatTime(job.created)[0] }}</span>
              </v-tooltip>
              {{ formatTime(job.created)[1] }} ago

            </td>
            <td>
              {{ job.algorithm }}
            </td>

          </tr>
          </tbody>
        </template>

      </v-simple-table>

    </template>
  </v-card>
</template>

<script>
import Utils from "../../scripts/Utils";

export default {
  name: "Jobs",
  gid: undefined,
  data() {
    return {
      jobs: [],
      graphjobs: [],
      jobsTabModel: 0,
      show: false,
    }
  },
  created() {
    this.$socket.$on("jobUpdateEvent", this.updateJob)
    this.reload()
  },
  methods: {
    reload: function () {
      this.gid = this.$route.params["gid"]
      this.loadJobs()
    },
    loadJobs: function () {
      this.jobs = []
      this.graphjobs = []
      this.$http.get("/getUserJobs?uid=" + this.$cookies.get("uid")).then(response => {
        if (response.data !== undefined)
          return response.data
      }).then(data => {
        data.forEach(job => {
          if (job.state !== "DONE")
            this.$socket.subscribeJob(job.jid, "jobUpdateEvent")
          this.jobs.push(job)
          if (job.basis === this.gid)
            this.graphjobs.push(job)
        })
        this.graphjobs.sort(this.sortJobs)
        this.jobs.sort(this.sortJobs)
      }).catch(console.log)

    },
    sortJobs: function (j1, j2) {
      return j2.created - j1.created
    },
    addJob: function (data) {
      this.$socket.subscribeJob(data.jid, "jobUpdateEvent")
      this.jobs.reverse()
      this.jobs.push(data)
      this.jobs.reverse()
      this.graphjobs.reverse()
      this.graphjobs.push(data)
      this.graphjobs.reverse()
      this.show = true
      // this.$refs.jobs.$forceUpdate()
    },
    downloadJob: function (job) {
      window.open('/backend/api/downloadJobResult?jid=' + job.jid)
    },


    updateJob: function (response) {
      let params = JSON.parse(response)
      this.jobs.forEach(j => {
        if (j.jid === params.jid) {
          j.state = params.state;
          j.gid = params.gid;
          j.algorithm = params.algorithm;
          j.target = params.target;
          j.update = params.update
          j.download = params.download
        }
      })
      if (params.state === 'DONE' || params.state==='TIMEOUT'||params.state==='LIMITED'||params.state==='ERROR') {
        console.log("job finished")
        this.$socket.unsubscribeJob(params.jid)
        this.$emit("reloadHistoryEvent")
      }
    }
    ,
    formatTime: function (timestamp) {
      return Utils.formatTime(timestamp)
    }
    ,

  }
}
</script>

<style scoped>

</style>
