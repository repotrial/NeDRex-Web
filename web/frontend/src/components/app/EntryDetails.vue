<template>
  <div :style="{maxWidth:maxWidth}">
    <template slot="progress">
      <v-progress-linear
        color="primary"
        height="5"
        indeterminate
      ></v-progress-linear>
    </template>

    <v-chip outlined v-if="redirected" @click="redirect()">
      <v-icon>fas fa-arrow-left</v-icon>
    </v-chip>
    <v-card-text>
      <template class="text--primary" style="font-size: x-large" v-if="detailedObject.node">
        <div class="text-h5">
          <v-tooltip left>
            <template v-slot:activator="{ on, attrs }">
              <v-icon
                left
                :color="getColoring('nodes',detailedObject['Type'])"
                v-bind="attrs"
                v-on="on"
                :size="hover.node1?'45px':'35px'"
                @mouseleave.native="hover.node1=false"
                @mouseover.native="hover.node1=true"
              >
                > fas fa-genderless
              </v-icon>
            </template>
            <span>{{ detailedObject['Type'] }}</span>
          </v-tooltip>
          {{ detailedObject['Name'] }}
        </div>

      </template>
      <template class="text--primary" style="font-size: x-large" v-if="detailedObject.edge">
        <div class="text-h5">
          <v-tooltip left>
            <template v-slot:activator="{ on, attrs }">
              <v-icon
                :color="getExtendedColoring('edges',detailedObject['Type'])[0]"
                v-bind="attrs"
                v-on="on"
                :size="hover.node1?'45px':'35px'"
                @mouseleave.native="hover.node1=false"
                @mouseover.native="hover.node1=true"
                @click="redirect( {edge:false,type:getExtendedNodeNames(detailedObject['Type'])[0],id:detailedObject['ID'].split('-')[0]},{type: 'edge', name:detailedObject['Type'],id:detailedObject['ID'].split('-')[0]})"
              >
                > fas fa-genderless
              </v-icon>
            </template>
            <span>{{ getExtendedNodeNames(detailedObject['Type'])[0] }}</span>
          </v-tooltip>
          {{ detailedObject['Node1'] }}
        </div>
        <div>
          <v-tooltip left>
            <template v-slot:activator="{ on, attrs }">
              <v-icon
                v-bind="attrs"
                v-on="on"
                :size="hover.arrow?'45px':'35px'"
                @mouseleave.native="hover.arrow=false"
                @mouseover.native="hover.arrow=true">
                {{ detailedObject.directed ? 'fas fa-long-arrow-alt-down' : 'fas fa-arrows-alt-v' }}
              </v-icon>
            </template>
            <span>{{ detailedObject['Type'] }}</span>
          </v-tooltip>
        </div>
        <div class="text-h5">
          <v-tooltip left>
            <template v-slot:activator="{ on, attrs }">
              <v-icon
                :color="getExtendedColoring('edges',detailedObject['Type'])[1]"
                v-bind="attrs"
                v-on="on"
                :size="hover.node2?'45px':'35px'"
                @mouseleave.native="hover.node2=false"
                @mouseover.native="hover.node2=true"
                @click="redirect( {edge:false,type:getExtendedNodeNames(detailedObject['Type'])[0],id:detailedObject['ID'].split('-')[0]},{type: 'edge', name:detailedObject['Type'],id:detailedObject['ID'].split('-')[0]})"
              >
                > fas fa-genderless
              </v-icon>
            </template>
            <span>{{ getExtendedNodeNames(detailedObject['Type'])[1] }}</span>
          </v-tooltip>
          {{ detailedObject['Node2'] }}
        </div>
      </template>
    </v-card-text>

    <v-divider></v-divider>
    <v-timeline align-top dense>
      <v-timeline-item small :color="getDetailDotColor(item)" v-for="item in detailedObject.order"
                       :key="item">

        <div><strong>{{ item }}</strong></div>
        <div>
          <v-list v-if="typeof detailedObject[item] === 'object'">
            <div v-for="(i, index) in detailedObject[item]" :key="index">
              <v-chip outlined v-if="getUrl(item,i).length>0" @click="openExternal(item,i)"
                      :title="getExternalSource(item,i)">
                {{ format(item, i) }}
                <v-icon right size="14px" :color="getExternalColor(item,i)">fas fa-external-link-alt
                </v-icon>
              </v-chip>
              <span v-else>{{ format(item, i) }}</span>
            </div>
          </v-list>
          <v-chip outlined v-else-if="getUrl(item,detailedObject[item]).length>0"
                  @click="openExternal(item,detailedObject[item])"
                  :title="getExternalSource(item,detailedObject[item])">
            {{ format(item, detailedObject[item]) }}
            <v-icon right size="14px" :color="getExternalColor(item,detailedObject[item])">fas
              fa-external-link-alt
            </v-icon>
          </v-chip>
          <span v-else>{{ format(item, detailedObject[item]) }}</span>
        </div>
      </v-timeline-item>
    </v-timeline>
  </div>
</template>

<script>
export default {
  name: "EntryDetails",
  props: {
    gid: String,
    entityGraph: Object,
    detailRequest:Object,
    maxWidth: String,
  },

  data() {
    return {
      detailedObject: {},
      redirected: false,
      lastReq: {},
      hover: {}
    }
  },

  created() {
    if(this.detailRequest!=null){
      this.loadDetails(this.detailRequest)
    }
  },


  methods: {


    format: function (item, value) {
      if (item === "SourceIDs" || item === "SourceID" || item === "TargetID" || item === "TargetIDs" || item === "MemberOne" || item === "MemberTwo") {
        let split = value.split(".")
        switch (split[0]) {
          case "entrez":
            return split[1]
          case "drugbank":
            return split[1]
          case "uniprot":
            return split[1]
          case "reactome":
            return split[1]
          case "mondo":
            return split[1]
          case "ncit":
            return split[1]
          case "mesh":
            return split[1]
          case "doid":
            return split[1]
          case "snomedct":
            return split[1]
          case "omim":
            return split[1]
          case "orpha":
            return split[1]
          case "umls":
            return split[1]
          case "meddra":
            return split[1]
          case "medgen":
            return split[1]
        }
      }
      if (item === "_cls")
        return value.split('.')[1]
      if (item === "mapLocation") {
        let split = value.indexOf("p") > 0 ? value.split("p") : value.split("q");
        return "Chr" + split[0] + ":" + split[1]
      }
      return value
    },
    getExternalSource: function (item, value) {
      if (item === "SourceIDs" || item === "SourceID" || item === "TargetID" || item === "TargetIDs" || item === "MemberOne" || item === "MemberTwo") {
        let split = value.split(".")
        switch (split[0]) {
          case "entrez":
            return "Entrez"
          case "drugbank":
            return "DrugBank"
          case "uniprot":
            return "UniProt"
          case "reactome":
            return "Reactome"
          case "mondo":
            return "Mondo"
          case "ncit":
            return "NCIthesaurus"
          case "mesh":
            return "Mesh"
          case "doid":
            return "DiseaseOntology"
          case "snomedct":
            return "SnomedCT"
          case "omim":
            return "OMIM"
          case "orpha":
            return "orpha"
          case "umls":
            return "NCImetathesaurus"
          case "meddra":
            return "BioPortal"
          case "medgen":
            return "NCBI"
        }
      }
      if (item === "ICD-10")
        return "ICD"
      if (item === "Symbol")
        return "GeneCards"
      if (item === "Genomic Location")
        return "UCSC"
      if (item === "CAS-Number")
        return "ChemIDplus"
      if (item === "Formula")
        return "ChemCalc"
      return value
    },
    getUrl: function (item, value) {
      let url = '';
      if (value === undefined || value.length === 0)
        return ""
      if (item === "SourceIDs" || item === "SourceID" || item === "TargetID" || item === "TargetIDs" || item === "MemberOne" || item === "MemberTwo") {
        let split = value.split(".")
        switch (split[0]) {
          case "entrez":
            return 'https://www.ncbi.nlm.nih.gov/gene/' + split[1]
          case "drugbank":
            return 'https://go.drugbank.com/drugs/' + split[1]
          case "uniprot":
            return 'https://www.uniprot.org/uniprot/' + split[1]
          case "reactome":
            return 'https://reactome.org/content/detail/' + split[1]
          case "mondo":
            return "https://monarchinitiative.org/disease/MONDO:" + split[1]
          case "ncit":
            return "https://ncit.nci.nih.gov/ncitbrowser/ConceptReport.jsp?dictionary=NCI_Thesaurus&code=" + split[1]
          case "mesh":
            return "https://meshb.nlm.nih.gov/record/ui?ui=" + split[1]
          case "doid":
            return "https://disease-ontology.org/term/DOID%3" + split[1]
          case "snomedct":
            return "http://snomed.info/id/" + split[1]
          case "omim":
            return "https://www.omim.org/entry/" + split[1]
          case "orpha":
            return "https://www.orpha.net/consor/cgi-bin/OC_Exp.php?lng=EN&Expert=" + split[1]
          case "umls":
            return "https://ncim.nci.nih.gov/ncimbrowser/ConceptReport.jsp?dictionary=NCI%20MetaThesaurus&code=" + split[1]
          case "meddra":
            return "http://purl.bioontology.org/ontology/MEDDRA/" + split[1]
          case "medgen":
            return "https://www.ncbi.nlm.nih.gov/medgen/?term=" + split[1]

        }
      }

      if (item === "ICD-10")
        return "https://icd.who.int/browse10/2019/en#/" + value
      if (item === "Symbol")
        return "https://www.genecards.org/cgi-bin/carddisp.pl?gene=" + value
      if (item === "Genomic Location")
        return "http://genome.ucsc.edu/cgi-bin/hgTracks?db=hg38&position=" + value
      if (item === "CAS-Number")
        return "https://chem.nlm.nih.gov/chemidplus/rn/" + value
      if (item === "Formula")
        return "https://www.chemcalc.org/?mf=" + value
      if (item === "Databases" || item === "Datasets" || item === "Primary Dataset")
        switch (value) {
          case "biogrid":
            return "https://thebiogrid.org/"
          case "hprd":
            return "https://www.hprd.org/"
          case "DrugBank":
            return "https://go.drugbank.com/"
          case "DrugCentral":
            return "https://drugcentral.org/"
          case "iid-pred":
            return "http://iid.ophid.utoronto.ca/"
          case "mint":
            return "https://mint.bio.uniroma2.it/"
          case "intact":
            return "https://www.ebi.ac.uk/intact/"
          case "iid":
            return "http://iid.ophid.utoronto.ca/"
          case "iid-ortho":
            return "http://iid.ophid.utoronto.ca/"
          case "bci":
            return "http://califano.c2b2.columbia.edu/b-cell-interactome"
          case "dip":
            return "https://dip.doe-mbi.ucla.edu/dip/Main.cgi"
          case "i2d":
            return "http://ophid.utoronto.ca/ophidv2.204/"
          case "innatedb":
            return "https://www.innatedb.com/"
        }
      if (item === "AssertedBy") {
        if (value === "omim")
          return "https://www.omim.org/"
        if (value === "disgenet")
          return "https://www.disgenet.org/"
      }
      return url;
    },
    getExternalColor: function (item, value) {
      if (item === "SourceIDs" || item === "SourceID" || item === "TargetID" || item === "TargetIDs" || item === "MemberOne" || item === "MemberTwo") {
        let split = value.split(".")
        switch (split[0]) {
          case "entrez":
            return "#369"
          case "drugbank":
            return "#ff00b8"
          case "uniprot":
            return "#5caecd"
          case "reactome":
            return "#2F9EC2"
          case "mondo":
            return "#15556a"
          case "ncit":
            return "#9f1314"
          case "mesh":
            return "#20558a"
          case "doid":
            return "#073399"
          case "snomedct":
            return "#00a9e0"
          case "omim":
            return "#333333"
          case "orpha":
            return "#d42c56"
          case "umls":
            return "#c31f40"
          case "meddra":
            return "#234979"
          case "medgen":
            return "#369"
        }
      }

      if (item === "ICD-10")
        return "#006000"
      if (item === "Symbol")
        return "#f07b05"
      if (item === "Genomic Location")
        return "#00457c"
      if (item === "CAS-Number")
        return "#749bc4"
      if (item === "Formula")
        return "#33484d"

      if (item === "Databases" || item === "Datasets" || item === "Primary Dataset")
        switch (value) {
          case "biogrid":
            return "773a3a"
          case "DrugBank":
            return "#ff00b8"
          case "DrugCentral":
            return "#e00600"
          case "iid-pred":
            return "#3a332d"
          case "hprd":
            return "#333466"
          case "mint":
            return "#228b22"
          case "intact":
            return "#57A7A7"
          case "iid":
            return "#3a332d"
          case "iid-ortho":
            return "#3a332d"
          case "bci":
            return "#4f54b0"
          case "dip":
            return "#a0c0f5"
          case "i2d":
            return "#3b3b6b"
          case "innatedb":
            return "#2572a6"
        }
      if (item === "assertedBy") {
        if (value === "omim")
          return "#333333"
        if (value === "disgenet")
          return "#ff00de"
      }
      return "black"
    }
    ,
    openExternal: function (item, i) {
      window.open(this.getUrl(item, i), '_blank')
    },
    redirect: function (req) {
      if (req!=null && req !== this.lastReq) {
        this.loadDetails(req,true)
      } else {
        this.redirected = false;
        this.loadDetails(this.lastReq)
      }
    },

    loadDetails: function (detailRequest, redirect) {
      this.redirected = redirect !=null && redirect
      if (!this.redirected)
        this.lastReq = detailRequest;
      if (!detailRequest.edge) {
        this.$http.get("getNodeDetails?name=" + detailRequest.type + "&id=" + detailRequest.id).then(response => {
          if (response.data !== undefined) {
            this.detailedObject = response.data
            this.detailedObject.node = true;
            this.description = "for " + this.detailedObject["Type"] + " " + this.detailedObject["Name"] + " (id:" + this.detailedObject["ID"] + ")"
          }
        }).catch(err => {
          console.error(err)
        })
      } else {
        this.$http.get("getEdgeDetails?name=" + detailRequest.type + "&id1=" + detailRequest.id1 + "&id2=" + detailRequest.id2 + "&gid=" + this.gid).then(response => {
          if (response.data !== undefined) {
            this.detailedObject = response.data
            this.detailedObject.edge = true;
            this.detailedObject.directed = this.$utils.directionExtended(this.entityGraph, detailRequest.type)
            if (this.detailedObject['Source'] !== undefined)
              this.description = "for " + detailRequest.type + " " + this.detailedObject['Name'] + " (id:" + this.detailedObject['Source'] + "->" + this.detailedObject['Target'] + ")"
            if (this.detailedObject['IDOne'] !== undefined)
              this.description = "for " + detailRequest.type + " id:" + this.detailedObject['IDOne'] + "<->" + this.detailedObject['IDTwo']
          }
        }).catch(err => {
          console.error(err)
        })
      }
    },

    getDetailDotColor: function (attribute) {
      if (this.detailedObject.node)
        return this.getColoring('nodes', this.detailedObject["Type"]);
      let basic = "#464e53";
      let colors = this.getExtendedColoring('edges', this.detailedObject["Type"]);
      if (["Source", "Node1", "SourceDomainID", "SourceID", "SourceDomainIDs", "IDOne", "MemberOne"].indexOf(attribute) > -1)
        return colors[0]
      if (["Target", "Node2", "TargetDomainID", "TargetID", "TargetDomainIDs", "IDTwo", "MemberTwo"].indexOf(attribute) > -1)
        return colors[1]
      return basic;
    },

    getColoring: function (type, name) {
      return this.$utils.getColoring(this.$global.metagraph, type, name)
    },

    getExtendedColoring: function (type, name, style) {
      return this.$utils.getColoringExtended(this.$global.metagraph, this.entityGraph, type, name, style)
    },
    directionExtended: function (edge) {
      let e = Object.values(this.entityGraph.edges).filter(e => e.name === edge)[0];
      if (e.node1 === e.node2)
        return 0
      return e.directed ? 1 : 2
    },
    getExtendedNodeNames: function (type) {
      return this.$utils.getNodesExtended(this.entityGraph, type)
    },


  }

}
</script>

<style scoped>

</style>
