import React, { useState } from 'react';
import {
  Box,
  Button,
  Card,
  CardContent,
  TextField,
  Typography,
  Link as MuiLink
} from '@mui/material';
import { MedicalServices, Person } from '@mui/icons-material';
import { Link } from 'react-router-dom';

const primaryBlue = '#2563eb';
const secondaryGreen = '#059669';

const ForgotPassword: React.FC = () => {
  const [email, setEmail] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState(false);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    // Simulate password reset
    setTimeout(() => {
      setLoading(false);
      if (!email) {
        setError('Please enter your email address.');
      } else {
        setSuccess(true);
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
        background: `linear-gradient(135deg, ${primaryBlue} 0%, ${secondaryGreen} 100%)`,
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
          color: 'rgba(37,99,235,0.08)',
        }}
      />
      <Card sx={{ minWidth: 350, maxWidth: 400, zIndex: 1, boxShadow: 6 }}>
        <CardContent>
          <Box sx={{ display: 'flex', justifyContent: 'center', mb: 2 }}>
            <Person sx={{ fontSize: 48, color: primaryBlue }} />
          </Box>
          <Typography variant="h5" align="center" fontWeight={700} color={primaryBlue} gutterBottom>
            Forgot Password
          </Typography>
          <Typography variant="body2" align="center" color="text.secondary" mb={2}>
            Enter your email address to reset your password
          </Typography>
          <form onSubmit={handleSubmit}>
            <TextField
              label="Email Address"
              variant="outlined"
              fullWidth
              margin="normal"
              value={email}
              onChange={e => setEmail(e.target.value)}
              autoComplete="email"
              type="email"
              required
            />
            {error && (
              <Typography color="error" variant="body2" sx={{ mt: 1 }}>
                {error}
              </Typography>
            )}
            {success && (
              <Typography color="success.main" variant="body2" sx={{ mt: 1 }}>
                Password reset link sent to your email.
              </Typography>
            )}
            <Button
              type="submit"
              variant="contained"
              color="primary"
              fullWidth
              sx={{ mt: 2, mb: 1, background: primaryBlue, '&:hover': { background: secondaryGreen } }}
              disabled={loading}
            >
              {loading ? 'Sending…' : 'Reset Password'}
            </Button>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mt: 1 }}>
              <MuiLink component={Link} to="/login" underline="hover" color={primaryBlue}>
                Back to Login
              </MuiLink>
              <MuiLink component={Link} to="/register" underline="hover" color={secondaryGreen}>
                Register
              </MuiLink>
            </Box>
          </form>
        </CardContent>
      </Card>
    </Box>
  );
};

export default ForgotPassword; 