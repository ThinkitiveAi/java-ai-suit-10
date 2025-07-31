import React from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Button,
  Grid
} from '@mui/material';
import { MedicalServices, Person } from '@mui/icons-material';
import { Link } from 'react-router-dom';

const primaryBlue = '#2563eb';
const secondaryGreen = '#059669';

const LoginChoice: React.FC = () => {
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
      <Card sx={{ minWidth: 400, maxWidth: 500, zIndex: 1, boxShadow: 6 }}>
        <CardContent>
          <Typography variant="h4" align="center" fontWeight={700} color={primaryBlue} gutterBottom>
            Welcome
          </Typography>
          <Typography variant="body1" align="center" color="text.secondary" mb={4}>
            Choose your login type to continue
          </Typography>
          <Grid container spacing={3}>
            <Grid size={{ xs:12, sm:6 }}>
              <Card
                component={Link}
                to="/login"
                sx={{
                  p: 3,
                  textAlign: 'center',
                  textDecoration: 'none',
                  color: 'inherit',
                  cursor: 'pointer',
                  transition: 'all 0.3s ease',
                  '&:hover': {
                    transform: 'translateY(-4px)',
                    boxShadow: 4,
                  },
                }}
              >
                <MedicalServices sx={{ fontSize: 48, color: primaryBlue, mb: 2 }} />
                <Typography variant="h6" fontWeight={600} color={primaryBlue} gutterBottom>
                  Provider Login
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  Medical professionals, doctors, and healthcare providers
                </Typography>
              </Card>
            </Grid>
            <Grid size={{ xs:12, sm:6 }}>
              <Card
                component={Link}
                to="/patient-login"
                sx={{
                  p: 3,
                  textAlign: 'center',
                  textDecoration: 'none',
                  color: 'inherit',
                  cursor: 'pointer',
                  transition: 'all 0.3s ease',
                  '&:hover': {
                    transform: 'translateY(-4px)',
                    boxShadow: 4,
                  },
                }}
              >
                <Person sx={{ fontSize: 48, color: secondaryGreen, mb: 2 }} />
                <Typography variant="h6" fontWeight={600} color={secondaryGreen} gutterBottom>
                  Patient Login
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  Patients seeking medical care and appointments
                </Typography>
              </Card>
            </Grid>
          </Grid>
          <Box sx={{ mt: 4, textAlign: 'center' }}>
            <Typography variant="body2" color="text.secondary">
              Don't have an account?{' '}
              <Link to="/register" style={{ color: primaryBlue, textDecoration: 'none' }}>
                Register here
              </Link>
            </Typography>
          </Box>
        </CardContent>
      </Card>
    </Box>
  );
};

export default LoginChoice;
