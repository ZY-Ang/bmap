import React from "react";
import { VectorMap } from '@south-paw/react-vector-maps';
import world from './world.json';
import './App.css';
import styled from 'styled-components';
import {
    subscribeDataForWord,
    subscribeInvocationDistribution,
    unsubscribeDataForWord,
    unsubscribeInvocationDistribution
} from "./database";


const Map = styled.div`
    margin: 0 auto;
    height: 100%;
    overflow-x: hidden;
  svg {
    stroke: #fff;

    // All layers are just path elements
    path {
      fill: #2b222c;
      cursor: pointer;
      outline: none;

      // When a layer is hovered
      &:hover {
        fill: rgba(168,43,43,0.83);
      }

      // When a layer is focused.
      &:focus {
        fill: rgba(168,43,43,0.6);
      }

      // You can also highlight a specific layer via it's id
      ${props => props.layers}
    }
  }
`;


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
        const { word } = this.state;
        if (!!this.wordDistributionListener) {
            unsubscribeDataForWord(this.state.previousWord, this.wordDistributionListener);
            this.setState({ previousWord: word });
        }
        this.wordDistributionListener = snapshot => this.setState(state => {
            const { invocationDistribution } = this.state;
            let wordDistribution = snapshot.val();
            if (!!wordDistribution && wordDistribution.constructor === Object && Object.keys(invocationDistribution).length > 0) {
                Object.keys(wordDistribution).map(key => (wordDistribution[key] /= invocationDistribution[key]));
                return { wordDistribution };
            } else return { wordDistribution: null };
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

    colorize(floatnumber) {
        if (floatnumber < 0.05) {
            return "#ffc9c5";
        } else if (floatnumber < 0.1) {
            return "#ffa9a3";
        } else if (floatnumber < 0.3) {
            return "#ff8d85";
        } else if (floatnumber < 0.6) {
            return "#ff675c";
        } else if (floatnumber < 0.8) {
            return "#ff493c";
        } else {
            return "#ff2f21";
        }
    }

    styleLayers(countryWeightMap) {
        let style = "";
        for (let key of Object.keys(countryWeightMap)) {
            style = style + "\n&[id = \"" + key.toLowerCase() + "\"] {fill: " + this.colorize(countryWeightMap[key]) + ";}";
        }
        console.log(style);
        return style;
    }


    render() {
        console.log(this.state.wordDistribution);
        let layersStyle = "";
        if (this.state.wordDistribution != null) {
            layersStyle = this.styleLayers(this.state.wordDistribution);
        }

        return (
            <div className="App">
                <form
                    style={{
                        position: 'absolute',
                        width: '100%',
                        height: '30%',
                        display: 'flex',
                        justifyContent: 'center',
                        alignItems: 'center'
                    }}
                    onSubmit={this.getWord}
                >
                    <input
                        type="text"
                        placeholder="hello"
                        value={this.state.word}
                        onChange={e => this.setState({ word: e.target.value })}
                    />
                </form>
                <Map layers={layersStyle}>
                    <VectorMap {...world}/>
                </Map>
            </div>
        );
    }
}

export default App;
