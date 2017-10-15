<template>
  <v-app id="example-1" dark>
    <v-toolbar fixed class="darken-2" dark>
    <v-btn icon to="/">
      <v-icon>arrow_back</v-icon>
    </v-btn>
      <v-toolbar-title>My Project</v-toolbar-title>
      <v-spacer></v-spacer>

      <v-flex xs3 sm3>
        <v-select id='select-label'
          prepend-icon="label"
          v-bind:items="datasets"
          v-model="selectedDataset"
          label="Select dataset"
          return-object
          :autocomplete="autocompleteLabels"
        ></v-select>
      </v-flex>
    <v-btn icon>
      <v-icon large>more_vert</v-icon>
    </v-btn>
    </v-toolbar>

    <main>
      <v-container>
          <!-- HTML BODY HERE -->
        <v-form>
          <v-flex xs3 md3>
            <v-text-field
              label="Model Name"
              v-model="modelName"
              required
            ></v-text-field>
          </v-flex>
          <v-flex xs3 md3>
            <v-text-field
              label="Description"
              v-model="modelDescription"
              required
            ></v-text-field>
          </v-flex>
          <v-flex xs12 sm6>
            <v-select
              label="Select"
              v-bind:items="diseases"
              v-model="selectedDiseases"
              multiple
              chips
              hint="What are the target diseases"
              persistent-hint
             ></v-select>
            </v-flex>
          <v-flex xs3 md3>
            <v-select
              v-bind:items="tissues"
              v-model="selectedTissue"
              label="Select Tissue"
              single-line
              bottom
            ></v-select>
          </v-flex>

          <v-flex xs3 md3>
            <input type="file">
            <v-btn class="btn btn-success btn-block" @click="upload">Upload Model</v-btn> (.yaml)
          </v-flex>
          <v-flex xs3 md3>
           <v-btn href="http://24.5.150.30:8097/">
              Submit
           </v-btn>
          </v-flex>
        </v-form>
          <!--<v-flex xs12 md8 offset-md1>
            <v-data-table
              v-bind:headers="headers"
              :items="items"
              hide-actions
              class="elevation-1"
            >
            <template slot="items" scope="props">
              <td>{{ props.item.name }}</td>
              <td class="text-xs-right">{{ props.item.calories }}</td>
              <td class="text-xs-right">{{ props.item.fat }}</td>
              <td class="text-xs-right">{{ props.item.carbs }}</td>
              <td class="text-xs-right">{{ props.item.protein }}</td>
              <td class="text-xs-right">{{ props.item.sodium }}</td>
              <td class="text-xs-right">{{ props.item.calcium }}</td>
              <td class="text-xs-right">{{ props.item.iron }}</td>
            </template>
            </v-data-table>
          </v-flex>-->
        </v-layout>
      </v-container>
    </main>

    <v-footer dark>
      <span class="white--text">© 2017</span>
    </v-footer>
  </v-app>

</template>

<script>

import RangeSlider from 'vue-range-slider'

import 'vue-range-slider/dist/vue-range-slider.css'

// Example Queries/Constants
import example from '../constants/example.js';
import { SAVE_OBJ_DETECT_IMAGE } from '../constants/graphql'
import { ALL_POST } from '../constants/graphql'
import { NEXT_OBJ_DETECT_IMG_QUERY } from '../constants/graphql'

var print = function(text) {
  console.log(text);
}

export default {
  name: 'createProject',
  components: {
    RangeSlider
  },
  props: [],

  data() {
    return {
      id: '',
      modelName: '',
      modelDescription: '',
      diseases: ['Lung Cancer', 'Melanoma'],
      selectedDiseases: '',
      tissues: ['Blood', 'Tumor'],
      selectedTissue: '',
      datasets: ['Genomic Sequence', 'MRI', 'EHR', 'ClinicalTrial', 'CT'],
      selectedDataset: 'Genomic Sequence',
      filename: ""
    }
  },

  apollo: {
    // This fires automatically on page load
  },

  computed: {
      autocompleteLabels: function () {
        return false;
    },
  },

  filters: {
    capitalize: function (value) {
      if (!value) return ''
      value = value.toString()
      return value.charAt(0).toUpperCase() + value.slice(1)
    }
  },

  mounted: function() {
    console.log("fires when vue mounted?");
  },

  created: function () {
    console.log("fires when vue created?");
  },

  methods: {
    getDatasets: function () {
      return this.datasets;
    },

    adjustThreshold: function () {
      console.log("Threshold: ", this.sliderValue);
    },

    getRandId: function () {
        return Math.random().toString(36).substr(2, 10);
    },

    // Example GET
    nextImage: function () {
      this.$apollo.queries.nextObjDetectImage.refetch();
    },

    // Example POST
    save: function () {
      let self = this;
      this.$apollo.mutate({
        mutation: ALL_POST,
        variables: {
          name: self.modelName,
          description: self.modelDescription,
          tissue: self.selectedTissue,
          dataset: self.selectedDataset
        }
      }).then((data) => {
        console.log("SUCCESSFULLY POSTED")
        //this.$apollo.queries.nextObjDetectImage.refetch();
      })
    },

    // onFileChange: function(e) {
    //     let files = e.target.files || e.dataTransfer.files;
    //     if (!files.length)
    //         return;
    //     this.createImage(files[0]);
    // },

    // upload: function(){
    //   console.log(this.file)
    //     // axios.post('/api/upload',{image: this.image}).then(response => {

    //     // });
    // }
  }
}
</script>



<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
h1, h2 {
  font-weight: normal;
}

ul {
  list-style-type: none;
  padding: 0;
}

li {
  display: inline-block;
  margin: 0 10px;
}

a {
  color: #42b983;
}

.slider {
  /* overwrite slider styles */
  width: 200px;
}
</style>
<style scoped>
    input[type=file] {
        position: absolute;
        left: -99999px;
    }
</style>

