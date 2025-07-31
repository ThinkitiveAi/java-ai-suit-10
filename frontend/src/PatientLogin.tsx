import React, { useState } from 'react';
import {
  Box,
  Button,
  Card,
  CardContent,
  Checkbox,
  FormControlLabel,
  IconButton,
  InputAdornment,
  TextField,
  Typography,
  Link as MuiLink
} from '@mui/material';
import { Visibility, VisibilityOff, MedicalServices, Person } from '@mui/icons-material';
import { Link } from 'react-router-dom';

const primaryBlue = '#2563eb';
const secondaryGreen = '#059669';

const PatientLogin: React.FC = () => {
  const [showPassword, setShowPassword] = useState(false);
  const [loginCredential, setLoginCredential] = useState('');
  const [password, setPassword] = useState('');
  const [rememberMe, setRememberMe] = useState(false);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleLogin = (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    // Simulate authentication
    setTimeout(() => {
      setLoading(false);
      if (!loginCredential || !password) {
        setError('Please enter your credentials.');
      } else {
        // Redirect to patient dashboard or handle login
      }
    }, 1200);
  };

  return (
    <Box
      sx={{
        minHeight: '100vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        background: `linear-gradient(135deg, ${secondaryGreen} 0%, ${primaryBlue} 100%)`,
        position: 'relative',
      }}
    >
      {/* Subtle medical icon background */}
      <MedicalServices
        sx={{
          position: 'absolute',
          top: 40,
          left: 40,
          fontSize: 120,
          color: 'rgba(5,150,105,0.08)',
        }}
      />
      <Card sx={{ minWidth: 350, maxWidth: 400, zIndex: 1, boxShadow: 6 }}>
        <CardContent>
          <Box sx={{ display: 'flex', justifyContent: 'center', mb: 2 }}>
            <Person sx={{ fontSize: 48, color: secondaryGreen }} />
          </Box>
          <Typography variant="h5" align="center" fontWeight={700} color={secondaryGreen} gutterBottom>
            Patient Login
          </Typography>
          <Typography variant="body2" align="center" color="text.secondary" mb={2}>
            Sign in to access your patient portal
          </Typography>
          <form onSubmit={handleLogin}>
            <TextField
              label="Email or Phone Number"
              variant="outlined"
              fullWidth
              margin="normal"
              value={loginCredential}
              onChange={e => setLoginCredential(e.target.value)}
              autoComplete="username"
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <Person color="action" />
                  </InputAdornment>
                ),
              }}
            />
            <TextField
              label="Password"
              variant="outlined"
              fullWidth
              margin="normal"
              type={showPassword ? 'text' : 'password'}
              value={password}
              onChange={e => setPassword(e.target.value)}
              autoComplete="current-password"
              InputProps={{
                endAdornment: (
                  <InputAdornment position="end">
                    <IconButton
                      aria-label="toggle password visibility"
                      onClick={() => setShowPassword(s => !s)}
                      edge="end"
                    >
                      {showPassword ? <VisibilityOff /> : <Visibility />}
                    </IconButton>
                  </InputAdornment>
                ),
              }}
            />
            <FormControlLabel
              control={
                <Checkbox
                  checked={rememberMe}
                  onChange={e => setRememberMe(e.target.checked)}
                  color="primary"
                />
              }
              label="Remember Me"
              sx={{ mt: 1 }}
            />
            {error && (
              <Typography color="error" variant="body2" sx={{ mt: 1 }}>
                {error}
              </Typography>
            )}
            <Button
              type="submit"
              variant="contained"
              color="primary"
              fullWidth
              sx={{ mt: 2, mb: 1, background: secondaryGreen, '&:hover': { background: primaryBlue } }}
              disabled={loading}
            >
              {loading ? 'Signing in…' : 'Login'}
            </Button>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mt: 1 }}>
              <MuiLink component={Link} to="/patient-forgot-password" underline="hover" color={secondaryGreen}>
                Forgot Password?
              </MuiLink>
              <MuiLink component={Link} to="/patient-register" underline="hover" color={primaryBlue}>
                Register
              </MuiLink>
            </Box>
          </form>
        </CardContent>
      </Card>
    </Box>
  );
};

export default PatientLogin;