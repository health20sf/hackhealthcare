<template>


  <v-app dark>
    <v-toolbar class="dark">
      <v-toolbar-title v-text="title"></v-toolbar-title>
    </v-toolbar>
    <main>
      <section>
        <!-- <v-parallax src="http://pre11.deviantart.net/3e10/th/pre/f/2017/144/8/0/fight_by_nesskain-dbaa5v1.jpg" height="600"> -->
        <!-- <v-parallax src="https://s3-us-west-1.amazonaws.com/labelai-static/hero.jpeg" height="600"> -->
        <v-parallax src="https://s3-us-west-1.amazonaws.com/labelai-static/health.jpg" height="600">
        
          <v-layout
            column
            align-center
            justify-center
            class="white--text"
          >
            <h1 class="white--text mb-2 display-3">Healthcare Model Training</h1>
            <div class="headline mb-3 text-xs-center">Label training data 10x faster with predictive clicking</div>
            <v-btn
              class="blue lighten-2 mt-5"
              dark
              large
              ripple
              to='/createProject'
            >
            Create Project
            </v-btn>
          </v-layout>
        </v-parallax>
      </section>

      <section>
        <v-layout
          column
          wrap
          class="my-5"
          align-center
        >
          <v-flex xs12>
            <v-container grid-list-xl>
              <v-layout row wrap align-center>
                <v-flex xs12 md4>
                  <v-card class="elevation-0 transparent">
                    <v-card-text class="text-xs-center">
                      <v-icon x-large class="blue--text text--lighten-2">color_lens</v-icon>
                    </v-card-text>
                    <v-card-title primary-title class="layout justify-center">
                      <div class="headline text-xs-center">For Researchers</div>
                    </v-card-title>
                    <v-card-text>
                      We provide analytics as a service based on specific data requests. Researchers get access to a bigger pool of data for increased model accuracy.
                    </v-card-text>
                  </v-card>
                </v-flex>
                <v-flex xs12 md4>
                  <v-card class="elevation-0 transparent">
                    <v-card-text class="text-xs-center">
                      <v-icon x-large class="blue--text text--lighten-2">build</v-icon>
                    </v-card-text>
                    <v-card-title primary-title class="layout justify-center">
                      <div class="headline text-xs-center">For Hospitals</div>
                    </v-card-title>
                    <v-card-text>
                      We clean the vast amounts of unstructured but highly valuable data, including EHR, clinical trial, genomics and PACS. 
                    </v-card-text>
                  </v-card>
                </v-flex>
                <v-flex xs12 md4>
                  <v-card class="elevation-0 transparent">
                    <v-card-text class="text-xs-center">
                      <v-icon x-large class="blue--text text--lighten-2">flash_on</v-icon>
                    </v-card-text>
                    <v-card-title primary-title class="layout justify-center">
                      <div class="headline">For Companies</div>
                    </v-card-title>
                    <v-card-text>
                      Our model training platform offers businesss the ability to train their models on our large anonymous datasets.
                    </v-card-text>
                  </v-card>
                </v-flex>
              </v-layout>
            </v-container>
          </v-flex>
        </v-layout>
      </section>

      <section>
        <v-parallax src="https://s3-us-west-1.amazonaws.com/labelai-static/section.jpeg" height="380">
          <v-layout column align-center justify-center>
            <div class="headline white--text mb-3">Train your Models on the biggest Healthcare dataset available</div>
            <em>Kick-start your machine learning project today</em>
            <v-btn
              class="blue lighten-2 mt-5"
              dark
              large
              href="/pre-made-themes"
            >
              Get Started
            </v-btn>
          </v-layout>
        </v-parallax>
      </section>

      <section>
        <v-container grid-list-xl>
          <v-layout row wrap justify-center class="my-5">
            <v-flex xs12 sm4>
              <v-card class="elevation-0 transparent">
                <v-card-title primary-title class="layout justify-center">
                  <div class="headline">Company info</div>
                </v-card-title>
                <v-card-text>
                  Cras facilisis mi vitae nunc lobortis pharetra. Nulla volutpat tincidunt ornare. 
                  Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas.
                </v-card-text>
              </v-card>
            </v-flex>
            <v-flex xs12 sm4 offset-sm1>
              <v-card class="elevation-0 transparent">
                <v-card-title primary-title class="layout justify-center">
                  <div class="headline">Contact us</div>
                </v-card-title>
                <v-list class="transparent">
                  <v-list-tile>
                    <v-list-tile-action>
                      <v-icon class="blue--text text--lighten-2">place</v-icon>
                    </v-list-tile-action>
                    <v-list-tile-content>
                      <v-list-tile-title>San Francisco, US</v-list-tile-title>
                    </v-list-tile-content>
                  </v-list-tile>
                  <v-list-tile>
                    <v-list-tile-action>
                      <v-icon class="blue--text text--lighten-2">email</v-icon>
                    </v-list-tile-action>
                    <v-list-tile-content>
                      <v-list-tile-title>bfortuner@gmail.com</v-list-tile-title>
                    </v-list-tile-content>
                  </v-list-tile>
                </v-list>
              </v-card>
            </v-flex>
          </v-layout>
        </v-container>
      </section>

      <v-footer class="blue darken-2">
        <v-layout row wrap align-center>
          <v-flex xs12>
            <div class="white--text ml-3">
              Made with
              <v-icon class="red--text">favorite</v-icon>
              in San Francisco</a>
            </div>
          </v-flex>
        </v-layout>
      </v-footer>
    </main>
  </v-app>  
</template>

<script>


export default {
  name: 'home',
  components: {},
  props: [],
  data () {
      return {
          drawer: true,
          title: 'MTAS.ai'
      }
  }
}
</script>


<style scoped>

</style>