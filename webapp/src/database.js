import firebase from "firebase/app";
import 'firebase/database';

/**
 * Front-end safe config.
 */
const firebaseConfig = {
    apiKey: "AIzaSyBU_v6uTSdh4KkW1Yu7WEsUaQUffPife88",
    authDomain: "buzzwordmap.firebaseapp.com",
    databaseURL: "https://buzzwordmap.firebaseio.com",
    projectId: "buzzwordmap",
    storageBucket: "buzzwordmap.appspot.com",
    messagingSenderId: "425916932994",
    appId: "1:425916932994:web:7d312451f236d5e276659b"
};
// Initialize Firebase
firebase.initializeApp(firebaseConfig);

// Get a reference to the database service
let db = firebase.database();

/**
 * Returns a JSON with the count of the number of calls to servers in various countries
 */
const getInvocationDistributionOnce = () => db.ref('/production/countries').once('value').then(snapshot => snapshot.val());

/**
 * Returns a JSON with the count of a particular
 * {@param word} after the promise resolves.
 *
 * This gets the data once.
 */
const getDataForWordOnce = word => db.ref(`/production/words/${word}`).once('value').then(snapshot => snapshot.val());

/**
 * Returns a JSON with the count of a particular
 * {@param word} but weighted between the total
 * calls to servers from various countries throughout
 * the world.
 *
 * This gets the data once. This is preferred over the
 * {@function getDataForWordOnce} as it displays a more
 * normalized output.
 */
export const getWeightedDataForWordOnce = async word => {
    const distribution = await getInvocationDistributionOnce();
    let data = await getDataForWordOnce(word);
    Object.keys(data).map(key => (data[key] = data[key] / distribution[key]));
    return data;
};

/**
 * Invokes {@param _function} which accepts a single
 * {@param snapshot} argument to obtain values from
 * the invocation distribution table when values change.
 */
export const subscribeInvocationDistribution = _function => db.ref('/production/countries').on('value', _function);

/**
 * Invokes {@param _function} which accepts a single
 * {@param snapshot} argument to obtain updated values
 * from the given {@param word} table when values change.
 */
export const subscribeDataForWord = (word, _function) => db.ref(`/production/words/${word}`).on('value', _function);
