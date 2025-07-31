import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import Navigation from './Navigation';
import PatientListView from './PatientListView';
import SettingsPage from './SettingsPage';
// Import Login component (to be created)
import Login from './Login';
import Register from './Register';
import ForgotPassword from './ForgotPassword';
import SetPassword from './SetPassword';
import LoginChoice from './LoginChoice';
import PatientLogin from './PatientLogin';
import PatientRegister from './PatientRegister';
const Dashboard = () => <div>Dashboard Page</div>;
const Appointments = () => <div>Appointments Page</div>;
const Reports = () => <div>Reports Page</div>;
// const Settings = () => <div>Settings Page</div>;

const App = () => (
  <BrowserRouter>
    <Routes>
      <Route path="/" element={<LoginChoice />} />
      <Route path="/provider/login" element={<Login />} />
      <Route path="/provider/register" element={<Register />} />
      <Route path="/provider/forgot-password" element={<ForgotPassword />} />
      <Route path="/provider/set-password" element={<SetPassword />} />
      <Route path="/patient/login" element={<PatientLogin />} />
      <Route path="/patient/register" element={<PatientRegister />} />
      
    </Routes>
  </BrowserRouter>
);

export default App;
