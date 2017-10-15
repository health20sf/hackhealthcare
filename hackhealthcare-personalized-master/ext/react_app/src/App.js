import React from 'react'
import ReactDOM from 'react-dom'
import $ from 'jquery'; 

export default class App extends React.Component{

  onError(error){
    console.log(error)
  }

  constructor(props) {
    super(props);
    this.state = {
            user: {
              firstname: "",
              lastname: "",
              dobday: '20',
              dobmonth: '01',
              dobyear: '1988',
              referrer: 'george',
              email: 'n.dunkel@gmail.com',
              phone: '(917) 704 3031',
              },
              loggedIn: false,
              results: [
                  {
                    id: '9403930493f',
                    type: 'Ophthalmologists',
                    name: 'Dr. Pickle D. Rick', 
                    address: '680 Haight Street', 
                    phone: '(917) 704 3031',
                    language: 'English, Spanish',
                    rating: '*****'
                  },
                  {
                    id: 'o2394kljl',
                    type: 'Ophthalmologists',
                    name: 'Dr. John A. Zoidberg', 
                    address: '250 George Street', 
                    phone: '(930) 450 375',
                    language: 'English',
                    rating: '****'
                  }
                ]
        }
      
  }

  componentDidMount() {
    
  } 


  render(){
    return (
                  
          <div>
              { !this.state.loggedIn ?
            <div>
              <h2 style={{color: "#00ad7e", display: 'inline'}}><b>doctors in touch</b></h2>
              <h4 style={{color: "#6c6c6c", display: 'inline'}}>&nbsp;&nbsp;&nbsp;Connecting you to personalized providers</h4>
              <hr />
              <h4 style={{paddingTop: 50}}><b>&nbsp; &nbsp; Good afternoon</b>, Dr. Lim!</h4>
              <div class="flex" style={{marginTop: 30}}>
                <div style={{maxWidth: 300}}>

                  <form id="form1">
                    Patient First Name<br />
                    <input
                      autoFocus
                      type="text"
                      class="form-control"
                      onChange={this.handleUsernameChange('firstname').bind(this)}
                      value={this.state.firstname}
                    /><br /><br />
                    Patient Last Name<br />
                    <input
                      class="form-control"
                      type="text"
                      value={this.state.lastname}
                    /><br /><br />
                    Date of Birth<br />
                    <input
                      type="date"
                      class="form-control"
                      value={this.state.dobyear}
                    /><br /><br />
                    Email<br />
                    <input
                      type="email"
                      class="form-control"
                      value={this.state.email}
                    /> <br /><br />
                    <button 
                    type="submit" 
                    form="form1" 
                    value="Submit"
                    style={{width: 300, backgroundColor: '#00ad7e'}}
                    className="submitButton"
                    type="submit"
                    class="btn btn-lg btn-success"
                    onClick={this.handleUsernameSubmit.bind(this)}>
                      <b>Refer {this.state.user.firstname} {this.state.user.lastname}</b>
                    </button>
                  </form>
                </div>
                <div class="border" style={{width: 100}}></div>
                <div style={{maxWidth: 300}}>
                  <form>
                    Patient ID<br />
                    <input
                      type="text"
                      class="form-control"
                    /><br /><br />
                  </form>
                </div>
              </div>
            </div>
            : null
          }
              { this.state.loggedIn ?
                <Results 
                results={this.state.results}
                patient={this.state.user}
                receivedData={this.receivedData} />
                : null
              }
            </div>
    )
  }

    handleUsernameChange () {
      return function (e) {
        var user = {};
        user.firstname = e.target.value.charAt(0).toUpperCase() + e.target.value.slice(1);
        this.setState({
            user: user
          })
      }.bind(this);
    }


    handleUsernameSubmit(e) {
        e.preventDefault();
        

        this.setState({
            loggedIn: true
          })
      }

    receivedData(response){
      return function () {
        this.setState({
              results: response
            })
      }.bind(this);
    }




}




class Results extends React.Component{

  componentDidMount() {
    $.ajax({
      url: 'http://www.hackhealthcare-personalized.info/results?id=25275',
      dataType: 'json',
      success: function(response) {
        this.props.receivedData(response)
        console.log(response)
      }.bind(this),
      error: function(xhr, status, err) {
        console.log('error!', xhr, status, err)
      }.bind(this)
    });
  }



  render(){

    return (
      <div>
      <div class="flex">
        <div>
          <h1>Ophthalmologists in {this.props.patient.firstname}s area </h1>
          <button style={{display: 'inline'}} class="btn btn-default"><span class="glyphicon glyphicon-remove"></span> Location: within 1 mile of {this.props.patient.firstname}s home</button> &nbsp;
          <button style={{display: 'inline'}} class="btn btn-default"><span class="glyphicon glyphicon-remove"></span> Insurance: covered by Aetna</button>
        </div>
      </div>
        <div style={{paddingTop: 50}}>
          {this.props.results.map(result => (
            <div key={result.id}>
              <div class="flex">
                <div class="nugget">
                  <h4>{result.name}</h4>
                  <p>{result.type}</p>
                  <p>{result.address}</p>
                  <p>{result.language}</p>
                  <p>{result.phone}</p><br />
                  <button class="btn btn-default"><span class="glyphicon glyphicon-heart"></span></button>&nbsp;&nbsp;
                  <button class="btn btn-default">Schedule now <span class="glyphicon glyphicon-menu-right"></span></button> &nbsp;
                  <button class="btn btn-success"><span class="glyphicon glyphicon-check"></span><b> Make Referral</b></button> &nbsp;
                  
                </div>
                <div class="nugget">
                  <iframe style={{border:0, height: 220}} src="https://www.google.com/maps/embed/v1/place?key=AIzaSyBz8soRKrBNMALn5zTxtH2grSVPbi2nSK4 
                    &q=Space+Needle,Seattle+WA" allowFullScreen>
                  </iframe><br /><br />
                </div>
                <div class="nugget">
                  <b>Avg. Rating</b>
                  <h5><span class="glyphicon glyphicon-star"></span><span class="glyphicon glyphicon-star"></span><span class="glyphicon glyphicon-star"></span><span class="glyphicon glyphicon-star"></span></h5>
                  <br /><br />
                  <b>Reviews</b>
                  <p>N/A</p>
                  <br /> <br />
                </div>
              </div>
              <hr />
              </div> ))}
          <div style={{marginLeft: 50}}>
          <p style={{textAlign: 'right', paddingTop:50}}><button class="btn btn-lg btn-info">
            <span class="glyphicon glyphicon-share"></span> Share list with {this.props.patient.firstname}
          </button>
          </p>
          </div>
        </div>
        
      </div>
    )
  }
}




ReactDOM.render(<App />, document.getElementById('root'));



