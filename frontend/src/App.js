import React, { useState, useRef, useEffect } from 'react';
import Table from './Table'
import validator from 'validator'
import './style.css'

const SERVICE_API_URL = `/service`;
function App() {
    const svcNameRef = useRef()
    const svcUrlRef = useRef()
    const [services, setServices] = useState([])
    const [errorMessage, setErrorMessage] = useState('')

    const fetchService = () => {
        console.log('fetching all services ')
        fetch(`${SERVICE_API_URL}`)
            .then(res => res.json())
            .then(json => setServices(json));
        console.log(services)
        
    }

    useEffect(() => {
        fetchService();
    }, []);

    const validateUrl = (value) => {

        if (!validator.isURL(value)) {
            setErrorMessage('invalid url')
        }
        else {
            setErrorMessage('')
        }
    }

    const validateName = (value) => {
        if (!validator.isAscii(value)) {
            setErrorMessage('invalid name')
        }
        else {
            setErrorMessage('')
        }
    }

    const fieldsComplete = (url, name) => {
        return (validator.isURL(url) && validator.isAscii(name))
    };


    function handleAdd(e) {

        const name = svcNameRef.current.value
        const url = svcUrlRef.current.value

        fetch(`${SERVICE_API_URL}`, {
            method: "POST",
            body: JSON.stringify({
                name: name,
                url: url
            }),
            headers: {
                "Content-type": "application/json; charset=UTF-8"
            }
        })
            .then(response => {
                // fetch the updated data
                if (response.ok) {
                    fetchService();
                }
                else {
                    console.log(response.status)
                }
                svcNameRef.current.value = null
                svcUrlRef.current.value = null
            })
    }


    return (
        <div className="App">
            <h2>Services Health Check Page</h2>
            <h3>Add new service</h3>
            <div>
                <input type="text" ref={svcNameRef} placeholder="Service name *" onChange={(e) => validateName(e.target.value)} />
                <input type="text" ref={svcUrlRef} placeholder="Service url *" onChange={(e) => validateUrl(e.target.value)} />
                <button onClick={handleAdd}>Add Service</button>
            </div>
            <span style={{color: 'red',}}>{errorMessage}</span>
            <Table data={services} key={'servicetable'} />
        </div>

    );
}
export default App;
