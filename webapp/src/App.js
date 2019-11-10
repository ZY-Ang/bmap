import React from "react";
import { VectorMap } from '@south-paw/react-vector-maps';
import world from './world.json';
import './App.css';
import {
    subscribeDataForWord,
    subscribeInvocationDistribution,
    unsubscribeDataForWord,
    unsubscribeInvocationDistribution
} from "./database";


class App extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            word: "hello",
            previousWord: "hello",
            invocationDistribution: {},
            wordDistribution: null
        };
    }



    componentDidMount() {
        this.invocationDistributionListener = snapshot => this.setState({
            invocationDistribution: snapshot.val()
        });
        subscribeInvocationDistribution(this.invocationDistributionListener);
        this.getWord();
    }

    getWord = e => {
        if (e) {
            e.preventDefault();
        }
        const {word} = this.state;
        if (!!this.wordDistributionListener) {
            unsubscribeDataForWord(this.state.previousWord, this.wordDistributionListener);
            this.setState({previousWord: word});
        }
        this.wordDistributionListener = snapshot => this.setState(state => {
            const {invocationDistribution} = this.state;
            let wordDistribution = snapshot.val();
            if (!!wordDistribution && wordDistribution.constructor === Object && Object.keys(invocationDistribution).length > 0) {
                Object.keys(wordDistribution).map(key => (wordDistribution[key] /= invocationDistribution[key]));
                return {wordDistribution};
            } else return {wordDistribution: null};
        });
        subscribeDataForWord(word, this.wordDistributionListener);
    };

    componentWillUnmount() {
        if (!!this.invocationDistributionListener) {
            unsubscribeInvocationDistribution(this.invocationDistributionListener);
        }
        if (!!this.wordDistributionListener) {
            unsubscribeDataForWord(this.state.previousWord, this.wordDistributionListener);
        }
    }

    render() {

        console.log(this.state.wordDistribution);
        return (
            <div className="App">
                <form
                    style={{
                        position: 'absolute',
                        width: '100%',
                        height: '30%',
                        display: 'flex',
                        justifyContent: 'left',
                        alignItems: 'center'
                    }}
                    onSubmit={this.getWord}
                >
                    <input
                        type="text"
                        placeholder="hello"
                        value={this.state.word}
                        onChange={e => this.setState({word: e.target.value})}
                    />
                </form>
                <VectorMap {...world} 
                
                    
                        />
            </div>
        );
    }
}

export default App;
