import React from 'react';
import ReactDOM from 'react-dom';
import './index.css';
import App from './App';
import * as serviceWorker from './serviceWorker';
import firebase from "firebase/app";
import 'firebase/database';

firebase.initializeApp({
    apiKey: "AIzaSyBU_v6uTSdh4KkW1Yu7WEsUaQUffPife88",
    authDomain: "buzzwordmap.firebaseapp.com",
    databaseURL: "https://buzzwordmap.firebaseio.com",
    projectId: "buzzwordmap",
    storageBucket: "buzzwordmap.appspot.com",
    messagingSenderId: "425916932994",
    appId: "1:425916932994:web:7d312451f236d5e276659b"
});

ReactDOM.render(<App />, document.getElementById('root'));

// If you want your app to work offline and load faster, you can change
// unregister() to register() below. Note this comes with some pitfalls.
// Learn more about service workers: https://bit.ly/CRA-PWA
serviceWorker.unregister();
